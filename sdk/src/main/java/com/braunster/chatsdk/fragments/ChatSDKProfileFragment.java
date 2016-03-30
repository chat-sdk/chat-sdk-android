package com.braunster.chatsdk.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractProfileFragment;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.SaveIndexDetailsTextWatcher;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKProfileFragment extends ChatSDKAbstractProfileFragment {


    private static final String TAG = ChatSDKProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;

    private static final String S_I_D_NAME = "saved_name_data";
    private static final String S_I_D_PHONE = "saved_phones_data";
    private static final String S_I_D_EMAIL = "saved_email_data";

    private EditText etName, etMail, etPhone;

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

        if (savedInstanceState != null)
        {
            if (DEBUG) Log.d(TAG, "Saved instance is not null");
            setDetails(getLoginType(), savedInstanceState);
        }
        else
        {
            if (DEBUG) Log.d(TAG, "Saved instance is null");
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

        // Setting a listener to text change, The listener will take cate of indexing the data.
        SaveIndexDetailsTextWatcher emailTextWatcher = new SaveIndexDetailsTextWatcher(BDefines.Keys.BEmail);
        SaveIndexDetailsTextWatcher nameTextWatcher= new SaveIndexDetailsTextWatcher(BDefines.Keys.BName);
        etMail.addTextChangedListener(emailTextWatcher);
        etName.addTextChangedListener(nameTextWatcher);


        // The number would only be index if phone index is enabled in BDefines.
        TextWatcher phoneTextWatcher = new SaveIndexDetailsTextWatcher(BDefines.Keys.BPhone);
        etPhone.addTextChangedListener(phoneTextWatcher);
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

        if (DEBUG) Log.v(TAG, "onSaveInstanceState, saving from local data.");
        outState.putString(S_I_D_NAME, etName.getText().toString());
        outState.putString(S_I_D_EMAIL, etMail.getText().toString());
        outState.putString(S_I_D_PHONE, etPhone.getText().toString());
    }

    @Override
    public void logout() {
        // Logout and return to the login activity.
        BFacebookManager.logout(getActivity());

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

        chatSDKProfileHelper.loadProfilePic(loginType);
    }

    private void setDetails(int loginType, Bundle bundle){
        etName.setText(bundle.getString(S_I_D_NAME));
        etPhone.setText(bundle.getString(S_I_D_PHONE));
        etMail.setText(bundle.getString(S_I_D_EMAIL));

        chatSDKProfileHelper.loadProfilePic(loginType);
    }

    /*############################################*/
}
