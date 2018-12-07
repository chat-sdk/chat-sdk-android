package co.chatsdk.android.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GossipGirlUsernameActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String city;
    private EditText nameEditText;
    private boolean usernameInUse;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gossipgirlusername);
        nameEditText = (EditText) findViewById(R.id.username_text);

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.city_spinner);

        //create a list of items for the spinner.
        String[] items = new String[] {
                "Sydney",
                "Melbourne",
                "Brisbane",
                "Perth",
                "Adelaide",
                "Gold Coast–Tweed Heads",
                "Newcastle–Maitland",
                "Canberra–Queanbeyan",
                "Sunshine Coast",
                "Wollongong",
                "Geelong",
                "Hobart",
                "Townsville",
                "Cairns",
                "Darwin",
                "Toowoomba",
                "Ballarat",
                "Bendigo",
                "Albury–Wodonga",
                "Mackay",
        };


        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        city = (String) parent.getItemAtPosition(position);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void continue_click(View v) {

        String username = nameEditText.getText().toString();

        // What happens if the name is blank?
        if (username.isEmpty()) {
            ToastHelper.show(GossipGirlUsernameActivity.this, "Please enter a username.");
            return;
        }

        String stageName = city + "-" + username;
        //The reason for this is that in firebase the stage name is written as city-username, whereas on screen it
        //should be presented as username-city.
        String presentedStageName = GossipGirlUserHelper.displayStageName(stageName);

        //Is this name already present?
        ChatSDK.search().usersForIndex(stageName, 1, Keys.StageName)
                .observeOn(AndroidSchedulers.mainThread()).doOnComplete(() -> {
                    //If so, then we have these options:
            if (usernameInUse) {
                AlertDialog.Builder alert = new AlertDialog.Builder(GossipGirlUsernameActivity.this);
                alert.setTitle("Stage name already in use");
                alert.setMessage("The stage name " + presentedStageName + " is already in use. You can file a request with the administrator that this be changed, or you can select a new stage name.");
                alert.setPositiveButton("Select a new stage name", new DialogInterface.OnClickListener() {
                    @Override
                    //The user can either pick a new name
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alert.setNegativeButton("Request this stage name", new DialogInterface.OnClickListener() {
                    @Override
                    //Or the user can petition to get the username they want.
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(GossipGirlUsernameActivity.this, NameChangePetitionActivity.class);
                        i.putExtra("stageNameTransfer", presentedStageName);
                        GossipGirlUsernameActivity.this.startActivity(i);
                    }
                });
                alert.show();
            } else {
                //Now the user must confirm this username and city.
                Intent i = new Intent (GossipGirlUsernameActivity.this, ConfirmUsernameActivity.class);
                i.putExtra(Keys.Username, username);
                i.putExtra(Keys.StageName, stageName);
                i.putExtra(Keys.PresentedStageName, presentedStageName);
                i.putExtra(Keys.City, city);
                startActivity(i);
            }
        }).subscribe(user -> {
            usernameInUse = true;
        }, throwable -> ToastHelper.show(GossipGirlUsernameActivity.this, throwable.getLocalizedMessage()));

    }
}
