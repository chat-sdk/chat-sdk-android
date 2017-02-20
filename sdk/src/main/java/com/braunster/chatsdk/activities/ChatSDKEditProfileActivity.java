/*
 * Created by Itzik Braun on 2/4/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 4/2/15 4:25 PM
 */

package com.braunster.chatsdk.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.countrypicker.Country;
import com.countrypicker.CountryPicker;
import com.countrypicker.CountryPickerListener;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by braunster on 02/04/15.
 */
public class ChatSDKEditProfileActivity extends ChatSDKBaseActivity implements OnClickListener {

    public static final String Male = "male", Female ="female";
    
    private TextView txtMale, txtFemale, txtDateOfBirth;
    
    private EditText etName, etLocation, etStatus;
    
    private ImageView imageCountryFlag;
    
    private boolean loggingOut = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        enableCheckOnlineOnResumed(true);
        
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.chatcat_activity_edit_profile);
        
        initViews();
        
        loadCurrentData();
    }

    private void initViews(){
        txtFemale = (TextView) findViewById(R.id.btn_female);
        txtMale = (TextView) findViewById(R.id.btn_male);
        txtDateOfBirth = (TextView) findViewById(R.id.txt_date_of_birth);
        
        etName = (EditText) findViewById(R.id.chat_sdk_et_name);
        etLocation = (EditText) findViewById(R.id.chat_sdk_et_location);
        etStatus = (EditText) findViewById(R.id.chat_sdk_et_status);

        imageCountryFlag = (ImageView) findViewById(R.id.chat_sdk_country_ic);
    }

    /**
     * Load the user data from the database.
     * */
    private void loadCurrentData(){
        BUser user = getNetworkAdapter().currentUserModel();
        
        String gender = user.metaStringForKey(BDefines.Keys.BGender);
        
        if (StringUtils.isEmpty(gender) || gender.equals(Male))
        {
            setSelected(txtFemale, false);

            setSelected(txtMale, true);
        }
        else
        {
            setSelected(txtMale, false);

            setSelected(txtFemale, true);
        }
        
        String countryCode = user.metaStringForKey(BDefines.Keys.BCountry);
        
        if (StringUtils.isNotEmpty(countryCode)){
            loadCountryFlag(countryCode);
        }
        
        String name = user.getMetaName();
        String location = user.metaStringForKey(BDefines.Keys.BLocation);
        String dateOfBirth = user.metaStringForKey(BDefines.Keys.BDateOfBirth);
        String status = user.metaStringForKey(BDefines.Keys.BStatus);

       if (StringUtils.isNotEmpty(name))
           etName.setText(name);

        if (StringUtils.isNotEmpty(location))
            etLocation.setText(location);

        if (StringUtils.isNotEmpty(dateOfBirth))
            txtDateOfBirth.setText(dateOfBirth);

        if (StringUtils.isNotEmpty(status))
            etStatus.setText(status);
    }
    
    private void loadCountryFlag(String countryCode){
        imageCountryFlag.setImageResource(Country.getResId(countryCode));
        imageCountryFlag.setVisibility(View.VISIBLE);
    }

    /**
     * Save the user details before closing the screen.
     * */
    private void saveDetailsBeforeClose(){
        BUser user = getNetworkAdapter().currentUserModel();




        if (!etName.getText().toString().isEmpty()) {
            user.setMetaName(etName.getText().toString());
        }

        user.setMetadataString(BDefines.Keys.BDateOfBirth, txtDateOfBirth.getText().toString());

        user.setMetadataString(BDefines.Keys.BStatus, etStatus.getText().toString());

        user.setMetadataString(BDefines.Keys.BLocation, etLocation.getText().toString());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        txtMale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.isSelected())
                    return;

                setSelected(txtFemale, false);

                setSelected(txtMale, true);

                getNetworkAdapter().currentUserModel().setMetadataString(BDefines.Keys.BGender, "male");
            }
        });

        txtFemale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.isSelected())
                    return;

                setSelected(txtMale, false);

                setSelected(txtFemale, true);

                getNetworkAdapter().currentUserModel().setMetadataString(BDefines.Keys.BGender, "female");
            }
        });

        findViewById(R.id.chat_sdk_logout_button).setOnClickListener(this);
        findViewById(R.id.chat_sdk_app_info_button).setOnClickListener(this);
        findViewById(R.id.chat_sdk_select_country_button).setOnClickListener(this);
        findViewById(R.id.chat_sdk_pick_birth_date_button).setOnClickListener(this);
    }

    public void logout() {
        // Logout and return to the login activity.
        BFacebookManager.logout(this);

        BNetworkManager.sharedManager().getNetworkAdapter().logout();
        chatSDKUiHelper.startLoginActivity(true);
    }
    
    private void setSelected(TextView textView, boolean selected){
        
        textView.setSelected(selected);
        
        if (selected)
            textView.setTextColor(getResources().getColor(R.color.white));
        else
            textView.setTextColor(getResources().getColor(R.color.dark_gray));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        if (!loggingOut)
        {
            saveDetailsBeforeClose();
            getNetworkAdapter().pushUser();
        }


        overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.chat_sdk_logout_button) {
            loggingOut = true;
            logout();
        }
        else if (i == R.id.chat_sdk_app_info_button) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);

                startActivity(intent);
            }
            catch (Exception e)
            {
                Timber.e(e.getCause(), getString(R.string.unable_to_open_app_in_settings));
                chatSDKUiHelper.showAlertToast(R.string.unable_to_open_app_in_settings);
            }

        }
        else if (i == R.id.chat_sdk_pick_birth_date_button) {
            final Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(ChatSDKEditProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    calendar.set(year, monthOfYear, dayOfMonth);

                    txtDateOfBirth.setText(new SimpleDateFormat(BDefines.Options.DateOfBirthFormat).format(calendar.getTime()));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        }
        else if (i == R.id.chat_sdk_select_country_button) {
            final CountryPicker picker = new CountryPicker();

            picker.setListener(new CountryPickerListener() {
                @Override
                public void onSelectCountry(String name, String code) {
                    getNetworkAdapter().currentUserModel().setMetadataString(BDefines.Keys.BCountry, code);
                    loadCountryFlag(code);
                    picker.dismiss();
                }
            });

            picker.show(getFragmentManager(), "");
        }
    }
}
