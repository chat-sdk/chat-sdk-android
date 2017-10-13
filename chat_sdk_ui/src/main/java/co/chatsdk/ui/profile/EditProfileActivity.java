package co.chatsdk.ui.profile;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.InterfaceManager;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.Cropper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 8/14/17.
 */

public class EditProfileActivity extends BaseActivity {

    public static final int PROFILE_PIC = 100;

    private SimpleDraweeView avatarImageView;
    private EditText statusEditText;
    private Spinner availabilitySpinner;
    private EditText nameEditText;
    private EditText locationEditText;
    private EditText phoneNumberEditText;
    private EditText emailEditText;
    private Button countryButton;
    private Button dateOfBirthButton;
    private Button logoutButton;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private HashMap<String, Object> userMeta;
    private String avatarURL;

    private User currentUser;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_edit_profile);

        String userEntityID = getIntent().getStringExtra(BaseInterfaceAdapter.USER_ENTITY_ID);

        if(userEntityID == null || userEntityID.isEmpty()) {
            showToast("User Entity ID not set");
            finish();
            return;
        }
        else {
            currentUser =  StorageManager.shared().fetchUserWithEntityID(userEntityID);

            // Save a copy of the data to see if it has changed
            userMeta = new HashMap<>(currentUser.metaMap());
        }
        initViews();


    }

    private void initViews() {

        avatarImageView = (SimpleDraweeView) findViewById(R.id.ivAvatar);
        statusEditText = (EditText) findViewById(R.id.etStatus);
        availabilitySpinner = (Spinner) findViewById(R.id.spAvailability);
        nameEditText = (EditText) findViewById(R.id.etName);
        locationEditText = (EditText) findViewById(R.id.etLocation);
        phoneNumberEditText = (EditText) findViewById(R.id.etPhone);
        emailEditText = (EditText) findViewById(R.id.etEmail);

        countryButton = (Button) findViewById(R.id.btnCountry);
        logoutButton = (Button) findViewById(R.id.btnLogout);
        dateOfBirthButton = (Button) findViewById(R.id.btnDateOfBirth);

        // Set the current user's information
        String status = currentUser.getStatus();
        String availability = currentUser.getAvailability();
        String name = currentUser.getName();
        String location = currentUser.getLocation();
        String phoneNumber = currentUser.getPhoneNumber();
        String email = currentUser.getEmail();
        String countryCode = currentUser.getCountryCode();
        String dateOfBirth = currentUser.getDateOfBirth();

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                EditProfileActivity.this.startActivityForResult(intent, PROFILE_PIC);
            }
        });

        avatarImageView.setImageURI(currentUser.getAvatarURL());

        if (StringUtils.isNotEmpty(countryCode)){
            Locale l = new Locale("", countryCode);
            countryButton.setText(l.getDisplayCountry());
        }

        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CountryPicker picker = CountryPicker.newInstance(getString(R.string.select_country));
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String countryCode, String phoneExtension, int i) {
                        countryButton.setText(name);
                        currentUser.setCountryCode(countryCode);
                        picker.dismiss();
                    }
                });
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

        if(!StringUtils.isEmpty(dateOfBirth)) {
             dateOfBirthButton.setText(dateOfBirth);
        }

        dateOfBirthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        try {
                            Date date = dateFormat.parse(day + "/" + month + "/" + year);
                            dateOfBirthButton.setText(dateFormat.format(date));
                            currentUser.setDateOfBirth(dateFormat.format(date));
                        }
                        catch (ParseException e) {
                            EditProfileActivity.this.showToast(e.getLocalizedMessage());
                        }
                    }
                };
                DatePickerDialog dialog = new DatePickerDialog(EditProfileActivity.this, listener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        statusEditText.setText(status);

        if(!StringUtils.isEmpty(availability)) {
            setAvailability(availability);
        }

        nameEditText.setText(name);
        locationEditText.setText(location);
        phoneNumberEditText.setText(phoneNumber);
        emailEditText.setText(email);


    }

    private void logout () {
        NM.auth().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
            @Override
            public void run() throws Exception {
                InterfaceManager.shared().a.startLoginActivity(getApplicationContext(), false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                Toast.makeText(EditProfileActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_save, 12, getString(R.string.action_save));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icn_24_save);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_save)
        {
            saveAndExit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_PIC)
        {
            if (resultCode == AppCompatActivity.RESULT_OK)
            {
                Uri uri = data.getData();

                Uri outputUri = Uri.fromFile(new File(this.getCacheDir(), "cropped.jpg"));
                Cropper crop = new Cropper(uri);

                Intent cropIntent = crop.getIntent(this, outputUri);
                int request = Crop.REQUEST_CROP;

                this.startActivityForResult(cropIntent, request);
            }
        }
        else  if (requestCode == Crop.REQUEST_CROP) {
            try {
                File image = new File(this.getCacheDir(), "cropped.jpg");
                avatarURL = image.getAbsolutePath();
                avatarImageView.setImageBitmap(new BitmapFactory().decodeFile(avatarURL));
                if(!StringUtils.isEmpty(avatarURL)) {
                    currentUser.setAvatarURL(avatarURL);
                }
            }
            catch (NullPointerException e){
                ToastHelper.show(this, R.string.unable_to_fetch_image);
            }
        }
    }

    private void saveAndExit () {

        String status = statusEditText.getText().toString();
        String availability = getAvailability();
        String name = nameEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String email = emailEditText.getText().toString();

        if(!StringUtils.isEmpty(status)) {
            currentUser.setStatus(status);
        }
        if(!StringUtils.isEmpty(availability)) {
            currentUser.setAvailability(availability);
        }
        if(!StringUtils.isEmpty(name)) {
            currentUser.setName(name);
        }
        if(!StringUtils.isEmpty(location)) {
            currentUser.setLocation(location);
        }
        if(!StringUtils.isEmpty(phoneNumber)) {
            currentUser.setPhoneNumber(phoneNumber);
        }
        if(!StringUtils.isEmpty(email)) {
            currentUser.setEmail(email);
        }

        currentUser.update();

        boolean changed = !userMeta.equals(currentUser.metaMap());
        boolean imageChanged = false;
        boolean presenceChanged = false;

        for(String key: currentUser.metaMap().keySet()) {
            if(key.equals(Keys.AvatarURL)) {
                imageChanged = valueChanged(currentUser.metaMap(), userMeta, key);
            }
            if(key.equals(Keys.Availability) || key.equals(Keys.Status)) {
                presenceChanged = presenceChanged || valueChanged(currentUser.metaMap(), userMeta, key);
            }
        }

        if(presenceChanged && !changed) {
            // Send presence
            NM.core().goOnline();
        }

        // TODO: Add this in for Firebase maybe move this to push user...
//        if(imageChanged && avatarURL != null) {
//            UserAvatarHelper.saveProfilePicToServer(avatarURL, this).subscribe();
//        }
//        else if (changed) {

        if(changed) {
            NM.core().pushUser()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }

        View v = getCurrentFocus();
        if(v instanceof EditText) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        finish();
    }

    private boolean valueChanged (Map<String, Object> h1, Map<String, Object> h2, String key) {
        Object o1 = h1.get(key);
        Object o2 = h2.get(key);
        if (o1 == null) {
            if (o2 != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return !o1.equals(o2);
        }
    }

    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }

    private String getAvailability () {
        String a = availabilitySpinner.getSelectedItem().toString().toLowerCase();
        if(a.equals("away")) {
            return Availability.Away;
        }
        else if(a.equals("extended away")) {
            return Availability.XA;
        }
        else if(a.equals("busy")) {
            return Availability.Busy;
        }
        else {
            return Availability.Available;
        }
    }

    private void setAvailability (String a) {
        String availability = "available";
        if(a.equals(Availability.Away)) {
            availability = "away";
        }
        else if(a.equals(Availability.XA)) {
            availability = "extended away";
        }
        else if(a.equals(Availability.Busy)) {
            availability = "busy";
        }
        availabilitySpinner.setSelection(getIndex(availabilitySpinner, availability));

    }

}
