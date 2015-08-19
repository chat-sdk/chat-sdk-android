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
import android.widget.LinearLayout;
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

import timber.log.Timber;

/**
 * Created by braunster on 02/04/15.
 */
public class ChatSDKEditProfileActivity extends ChatSDKBaseActivity implements OnClickListener {
    
    private TextView txtMale, txtFemale, txtEveryone, txtNobody, txtFriends;
    
    private EditText etName, etLocation, etStatus;
    
    private ImageView imageCountryFlag;

    private Button btnSelectBirthday;

    private LinearLayout btnSelectCountry;

    private boolean loggingOut = false;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(BDefines.Options.DateOfBirthFormat);
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
        txtEveryone = (TextView) findViewById(R.id.btn_everyone);
        txtNobody = (TextView) findViewById(R.id.btn_nobody);
        txtFriends= (TextView) findViewById(R.id.btn_friends);
        
        etName = (EditText) findViewById(R.id.chat_sdk_et_name);
        etLocation = (EditText) findViewById(R.id.chat_sdk_et_location);
        etStatus = (EditText) findViewById(R.id.chat_sdk_et_status);

        btnSelectBirthday = (Button)findViewById(R.id.chat_sdk_pick_birth_date_button);
        btnSelectCountry = (LinearLayout) findViewById(R.id.chat_sdk_linear_select_country);

        imageCountryFlag = (ImageView) findViewById(R.id.chat_sdk_ic_country);
    }

    /**
     * Load the user data from the database.
     * */
    private void loadCurrentData(){
        BUser user = getNetworkAdapter().currentUserModel();

        // Setting up user gender, Default is male.
        String gender = user.metaStringForKey(BDefines.Keys.BGender);
        if (StringUtils.isEmpty(gender) || gender.equals(BDefines.Keys.BMale))
        {
            setSelected(txtFemale, false);

            setSelected(txtMale, true);
        }
        else
        {
            setSelected(txtMale, false);

            setSelected(txtFemale, true);
        }

        // Setting up the country code selected
        String countryCode = user.metaStringForKey(BDefines.Keys.BCountry);
        if (StringUtils.isNotEmpty(countryCode)){
            loadCountryFlag(countryCode);
        }

        String allowInvites = user.metaStringForKey(BDefines.Keys.BAllowInvites);

        if (StringUtils.isEmpty(allowInvites))
            allowInvites = BDefines.Keys.BEveryone;

        switch (allowInvites)
        {
            case BDefines.Keys.BFriends:
                setSelected(txtFriends, true);
                break;

            case BDefines.Keys.BNobody:
                setSelected(txtNobody, true);
                break;

            case BDefines.Keys.BEveryone:
                setSelected(txtEveryone, true);
                break;
        }

        String name = user.getName();
        String location = user.metaStringForKey(BDefines.Keys.BCity);
        String dateOfBirth = user.metaStringForKey(BDefines.Keys.BDateOfBirth);
        String status = user.metaStringForKey(BDefines.Keys.BDescription);

       if (StringUtils.isNotEmpty(name))
           etName.setText(name);

        if (StringUtils.isNotEmpty(location))
            etLocation.setText(location);

        if (StringUtils.isNotEmpty(dateOfBirth))
        {

            btnSelectBirthday.setTag(String.valueOf(Long.parseLong(dateOfBirth)));
            btnSelectBirthday.setText(simpleDateFormat.format(Long.parseLong(dateOfBirth)));
        }

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
            user.setName(etName.getText().toString());
        }

        user.setMetadataString(BDefines.Keys.BDateOfBirth, (String) btnSelectBirthday.getTag());

        user.setMetadataString(BDefines.Keys.BDescription, etStatus.getText().toString());

        user.setMetadataString(BDefines.Keys.BCity, etLocation.getText().toString());

        getNetworkAdapter().currentUserModel()
                .setMetadataString(
                        BDefines.Keys.BGender, txtMale.isSelected() ? BDefines.Keys.BMale : BDefines.Keys.BFemale);

        getNetworkAdapter().currentUserModel()
                .setMetadataString(
                        BDefines.Keys.BAllowInvites, txtFriends.isSelected() ?
                                BDefines.Keys.BFriends : txtEveryone.isSelected() ?
                                BDefines.Keys.BEveryone : BDefines.Keys.BNobody);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        txtMale.setOnClickListener(genderClickListener);
        txtFemale.setOnClickListener(genderClickListener);

        txtEveryone.setOnClickListener(invitesClickListener);
        txtFriends.setOnClickListener(invitesClickListener);
        txtNobody.setOnClickListener(invitesClickListener);

        findViewById(R.id.chat_sdk_logout_button).setOnClickListener(this);
        findViewById(R.id.chat_sdk_app_info_button).setOnClickListener(this);
        btnSelectCountry.setOnClickListener(this);
        btnSelectBirthday.setOnClickListener(this);
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

            getNetworkAdapter().updateIndexForUser(getNetworkAdapter().currentUserModel());

            getNetworkAdapter().pushUser();
        }


        overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }


    private OnClickListener genderClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isSelected())
                return;

            boolean male = v.equals(txtMale);

            setSelected(txtMale, male);

            setSelected(txtFemale, !male);
        }
    };

    private OnClickListener invitesClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isSelected())
                return;

            setSelected(txtFriends, v.equals(txtFriends));
            setSelected(txtEveryone, v.equals(txtEveryone));
            setSelected(txtNobody, v.equals(txtNobody));
        }
    };

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

                    btnSelectBirthday.setTag(String.valueOf(calendar.getTime().getTime()));
                    btnSelectBirthday.setText(simpleDateFormat.format(calendar.getTime()));

                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        }
        else if (i == R.id.chat_sdk_linear_select_country) {
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
