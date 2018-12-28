package com.raymond.gossipgirl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.utils.ToastHelper;
import io.keiji.plistparser.PListArray;
import io.keiji.plistparser.PListDict;
import io.keiji.plistparser.PListException;
import io.keiji.plistparser.PListParser;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GGUsernameActivity extends AppCompatActivity {

    private String country;
    private String city;
    private EditText nameEditText;
    private boolean usernameInUse;

    private DisposableList disposableList = new DisposableList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        nameEditText = findViewById(R.id.username_text);

        Spinner countrySpinner = findViewById(R.id.country_spinner);
        Spinner citySpinner = findViewById(R.id.city_spinner);

        Map<String, List<String>> countries = getCountries();
        String[] countriesArray = countries.keySet().toArray(new String[0]);

        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countriesArray);

        countrySpinner.setAdapter(countriesAdapter);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                country = (String) adapterView.getItemAtPosition(i);
                List<String> citiesList = countries.get(country);
                if (citiesList != null) {
                    String[] citiesArray = citiesList.toArray(new String[0]);
                    ArrayAdapter<String> citiesAdapter = new ArrayAdapter<>(GGUsernameActivity.this, android.R.layout.simple_spinner_dropdown_item, citiesArray);
                    citySpinner.setAdapter(citiesAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                city = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    Map<String, List<String>> getCountries() {
        Map<String, List<String>> countries = new HashMap<>();
        InputStream inputStream = getResources().openRawResource(R.raw.countries);
        try {
            PListArray countriesArray = PListParser.parse(inputStream);
            for (int i = 0; i < countriesArray.size(); i++) {
                PListDict countryDict = (PListDict) countriesArray.get(i);
                String countryName = countryDict.getString("name");
                List<String> cities = new ArrayList<>();
                PListArray citiesArray = countryDict.getPListArray("cities");
                for (int j = 0; j < citiesArray.size(); j++) {
                    cities.add(citiesArray.getString(j));
                }

                countries.put(countryName, cities);

            }
        } catch (PListException e) {
            e.printStackTrace();
        }
        return countries;
    }

    public void didClickOnContinue(View v) {

        String username = nameEditText.getText().toString();

        // What happens if the name is blank?
        if (username.isEmpty()) {
            ToastHelper.show(getApplicationContext(), "Please enter a username.");
            return;
        }

        String stageName = city + "-" + username;
        //The reason for this is that in firebase the stage name is written as city-username, whereas on screen it
        //should be presented as username-city.
        String presentedStageName = GGUserHelper.displayStageName(stageName);

        //Is this name already present?
        disposableList.add(ChatSDK.search().usersForIndex(stageName, 1, GGKeys.StageName)
                .observeOn(AndroidSchedulers.mainThread()).doOnComplete(() -> {
                    //If so, then we have these options:
            if (usernameInUse) {
                AlertDialog.Builder alert = new AlertDialog.Builder(GGUsernameActivity.this);
                alert.setTitle("Stage name already in use");
                alert.setMessage("The stage name " + presentedStageName + " is already in use. You can file a request with the administrator that this be changed, or you can select a new stage name.");
                //The user can either pick a new name
                alert.setPositiveButton("Select a new stage name", (dialog, which) -> {});
                //Or the user can petition to get the username they want.
                alert.setNegativeButton("Request this stage name", (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), GGNameChangePetitionActivity.class);
                    intent.putExtra("stageNameTransfer", presentedStageName);
                    ChatSDK.ui().startActivity(getApplicationContext(), intent);
                });
                alert.show();
            } else {
                //Now the user must confirm this username and city.
                Intent intent = new Intent (getApplicationContext(), GGConfirmUsernameActivity.class);
                intent.putExtra(Keys.Location, city);
                intent.putExtra(GGKeys.Username, username);
                intent.putExtra(GGKeys.Country, country);
                intent.putExtra(GGKeys.StageName, stageName);
                intent.putExtra(GGKeys.PresentedStageName, presentedStageName);
                ChatSDK.ui().startActivity(getApplicationContext(), intent);
            }
        }).subscribe(user -> {
            usernameInUse = true;
        }, throwable -> ToastHelper.show(GGUsernameActivity.this, throwable.getLocalizedMessage())));
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposableList.dispose();
    }

}
