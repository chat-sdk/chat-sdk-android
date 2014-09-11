package com.braunster.chatsdk.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.fragments.abstracted.AbstractProfileFragment;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.facebook.Session;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKProfileFragment extends AbstractProfileFragment implements TextView.OnEditorActionListener{


    private static final String TAG = ChatSDKProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;

    private static final String S_I_F_NAME = "saved_name_flag";
    private static final String S_I_F_PHONE = "saved_phones_flag";
    private static final String S_I_F_EMAIL = "saved_email_flag";
    private static final String S_I_F_PROFILE = "saved_profile_flag";

    private static final String S_I_D_NAME = "saved_name_data";
    private static final String S_I_D_PHONE = "saved_phones_data";
    private static final String S_I_D_EMAIL = "saved_email_data";

    private EditText etName, etMail, etPhone;
    private boolean isNameTouched = false, isEmailTouched = false, isPhoneTouched = false,          isProfilePicChanged;

    public static ChatSDKProfileFragment newInstance() {
        ChatSDKProfileFragment f = new ChatSDKProfileFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (DEBUG) Log.d(TAG, "onCreateView");

        initViews(inflater);

        loginType = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);

        if (savedState != null)
        {
            Log.d(TAG, "Saved State is not null");
            isEmailTouched = savedState.getBoolean(S_I_F_EMAIL);
            isNameTouched = savedState.getBoolean(S_I_F_NAME);
            isPhoneTouched = savedState.getBoolean(S_I_F_PHONE);
            isProfilePicChanged = savedState.getBoolean(S_I_F_PROFILE);

            setDetails(loginType, savedState);
        }
        else if (savedInstanceState != null)
        {
            Log.d(TAG, "Saved instance is not null");
            isEmailTouched = savedInstanceState.getBoolean(S_I_F_EMAIL);
            isNameTouched = savedInstanceState.getBoolean(S_I_F_NAME);
            isPhoneTouched = savedInstanceState.getBoolean(S_I_F_PHONE);
            isProfilePicChanged = savedInstanceState.getBoolean(S_I_F_PROFILE);

            setDetails(loginType, savedInstanceState);
        }
        else
        {
            Log.d(TAG, "Saved instance is null");
            loadData();
        }

        return mainView;
    }

    public void initViews(LayoutInflater inflater){
        if (inflater != null)
            mainView = inflater.inflate(R.layout.chat_sdk_activity_profile, null);
        else return;

        super.initViews();

        setupTouchUIToDismissKeyboard(mainView, R.id.chat_sdk_circle_ing_profile_pic);

        // Changing the weight of the view according to orientation.
        // This will make sure (hopefully) there is enough space to show the views in landscape mode.
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            if (DEBUG) Log.d(TAG, "Landscape");
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.linear).getLayoutParams();
            layoutParams.weight = 3;
            mainView.findViewById(R.id.linear).setLayoutParams(layoutParams);
        }
        else
        {
            if (DEBUG) Log.d(TAG, "Portrait");
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.linear).getLayoutParams();
            layoutParams.weight = 2;
            mainView.findViewById(R.id.linear).setLayoutParams(layoutParams);
        }

        etName = (EditText) mainView.findViewById(R.id.chat_sdk_et_name);
        etMail = (EditText) mainView.findViewById(R.id.chat_sdk_et_mail);
        etPhone = (EditText) mainView.findViewById(R.id.chat_sdk_et_phone_number);
    }

    @Override
    public void onResume() {
        super.onResume();

        //region Listening to text changes.
        etName.setOnEditorActionListener(this);
        etPhone.setOnEditorActionListener(this);
        etMail.setOnEditorActionListener(this);

        etMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.e(TAG, "onClick");
//                if (v.getId() == R.id.chat_sdk_et_name)
//                    isNameTouched = true;
//                else if (v.getId() == R.id.chat_sdk_et_mail)
//                    isEmailTouched = true;
//                else if (v.getId() == R.id.chat_sdk_et_phone_number)
//                    isPhoneTouched = true;
                isEmailTouched = true;
            }
        });
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (DEBUG) Log.e(TAG, "After text changed");
                isNameTouched = true;
            }
        });
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {;
            }

            @Override
            public void afterTextChanged(Editable s) {
                isPhoneTouched = true;
            }
        });
        //endregion
    }

    @Override
    public void loadData() {
        super.loadData();
        setDetails((Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey));
    }

    @Override
    public void clearData() {
        super.clearData();

        if (mainView != null)
        {
            etName.getText().clear();
            etMail.getText().clear();
            etPhone.getText().clear();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        //http://stackoverflow.com/a/15314508/2568492
        if (mainView == null)
        {
            if (savedState == null)
                return;

            if (DEBUG) Log.v(TAG, "onSaveInstanceState, Saving from saved state");
            outState.putBoolean(S_I_F_NAME, savedState.getBoolean(S_I_F_NAME));
            outState.putBoolean(S_I_F_EMAIL, savedState.getBoolean(S_I_F_EMAIL));
            outState.putBoolean(S_I_F_PHONE, savedState.getBoolean(S_I_F_PHONE));
            outState.putBoolean(S_I_F_PROFILE, savedState.getBoolean(S_I_F_PROFILE));

            outState.putString(S_I_D_NAME, savedState.getString(S_I_D_NAME) );
            outState.putString(S_I_D_EMAIL, savedState.getString(S_I_D_EMAIL));
            outState.putString(S_I_D_PHONE, savedState.getString(S_I_D_PHONE));

            savedState = null;
            return;
        }

        if (DEBUG) Log.v(TAG, "onSaveInstanceState, saving from local data.");
        outState.putBoolean(S_I_F_NAME, isNameTouched);
        outState.putBoolean(S_I_F_EMAIL, isEmailTouched);
        outState.putBoolean(S_I_F_PHONE, isPhoneTouched);
        outState.putBoolean(S_I_F_PROFILE, isProfilePicChanged);

        outState.putString(S_I_D_NAME, etName.getText().toString());
        outState.putString(S_I_D_EMAIL, etMail.getText().toString());
        outState.putString(S_I_D_PHONE, etPhone.getText().toString());
    }

    @Override
    public void logout() {
        // Logout and return to the login activity.

        if (Session.getActiveSession() != null)
        {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
        else
        {
            if (DEBUG) Log.e(TAG, "getActiveSessionIsNull");
            Session session = Session.openActiveSessionFromCache(getActivity());

            if (session != null)
                session.closeAndClearTokenInformation();
        }

        BNetworkManager.sharedManager().getNetworkAdapter().logout();
        chatSDKUiHelper.startLoginActivity(true);
    }

    /*############################################*/
    /* UI*/
    /** Fetching the user details from the user's metadata.*/
    private void setDetails(int loginType){
        if (mainView == null || getActivity() == null)
        {
            return;
        }

        BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
        etName.setText(user.getMetaName());
        etPhone.setText(user.metaStringForKey(BDefines.Keys.BPhone));
        etMail.setText(user.getMetaEmail());

        loadProfilePic(loginType);
    }

    private void setDetails(int loginType, Bundle bundle){
        etName.setText(bundle.getString(S_I_D_NAME));
        etPhone.setText(bundle.getString(S_I_D_PHONE));
        etMail.setText(bundle.getString(S_I_D_EMAIL));

        loadProfilePic(loginType);
    }

    /*############################################*/
    @Override
    public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
        /* Logic:
        *  First we will remove the index from the index list, To find the index we are using the old metadata value.
        *  After it is deleted we update the metadata locally and then push the user back to the server.*/
        if (actionId == EditorInfo.IME_ACTION_DONE)
        {
            if (v.getId() == R.id.chat_sdk_et_name){
                isNameTouched = true;
            }
            else if (v.getId() == R.id.chat_sdk_et_mail){
                isEmailTouched = true;
            }
            else if (v.getId() == R.id.chat_sdk_et_phone_number){
                isPhoneTouched = true;
            }
        }

        updateProfileIfNeeded();

        return false;
    }

    public void updateProfileIfNeeded(){
        boolean toUpdate = isEmailTouched || isPhoneTouched || isNameTouched || isProfilePicChanged;

        /*if (DEBUG) Log.v(TAG, "updateProfileIfNeeded, toUpdate: " + toUpdate);

        if (DEBUG) Log.v(TAG, "updateProfileIfNeeded " + (isEmailTouched?",Email ":"") + (isNameTouched?",Name ":"") + (isPhoneTouched?",Phone ":"") + (isProfilePicChanged?",Pic Changed":""));*/

//        if (!toUpdate)
//            return;

        List<BMetadata> metadataToPush = new ArrayList<BMetadata>();

        // The current user.
        final BUser bUser =BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        if (isPhoneTouched)
        {
            String phone = etPhone.getText().toString();
            String curPhone = bUser.metaStringForKey(BDefines.Keys.BPhone);

            if (!phone.equals(curPhone))
            {
                bUser.setMetadataString(BDefines.Keys.BPhone, phone);
                metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPhone, BMetadata.Type.STRING));
                indexUser(bUser, curPhone, phone);
            }

            isPhoneTouched = false;
        }

        if (isNameTouched)
        {
            String name = etName.getText().toString();
            String curName = bUser.getMetaName();
            if (!name.equals(curName))
            {
                bUser.setMetaName(name);
                metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BName, BMetadata.Type.STRING));
                indexUser(bUser, curName, name);
            }

            if (StringUtils.isEmpty(bUser.getMetadataForKey(BDefines.Keys.BPictureURL, BMetadata.Type.STRING).getValue()))
                setInitialsProfilePic();

            isNameTouched = false;
        }

        if (isEmailTouched)
        {
            String email = etMail.getText().toString();
            String curEmail = bUser.getMetaEmail();
            if (!email.equals(curEmail))
            {
                bUser.setMetaEmail(email);
                metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BEmail, BMetadata.Type.STRING));
                indexUser(bUser, curEmail, email);
            }

            isEmailTouched = false;
        }

        if (isProfilePicChanged)
        {
//            metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPicture, BMetadata.Type.IMAGE));
            metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPictureURL, BMetadata.Type.STRING));
            metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPictureURLThumbnail, BMetadata.Type.STRING));
            isProfilePicChanged = false;
        }

        // Push the changed metadata.
        for (BMetadata metadata : metadataToPush)
            BFirebaseInterface.pushEntity(metadata, null);
    }

}
