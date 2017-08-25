/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.profile;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.ui.R;

import co.chatsdk.ui.helpers.SaveIndexDetailsTextWatcher;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.UserAvatarHelper;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by itzik on 6/17/2014.
 */
@Deprecated
public class ProfileFragment2 extends AbstractProfileFragment {

    private static final String TAG = ProfileFragment2.class.getSimpleName();

    private EditText etName, etMail, etPhone;

    public static ProfileFragment2 newInstance() {
        ProfileFragment2 f = new ProfileFragment2();
        Bundle b = new Bundle();
        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        NM.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated)).subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                if(networkEvent.user.equals(NM.currentUser())) {
                    loadData();
                }
            }
        });

        initViews(inflater);
        loadData();

        return mainView;
    }

    public void initViews(LayoutInflater inflater){
        mainView = inflater.inflate(R.layout.chat_sdk_activity_profile, null);

        super.initViews();

        setupTouchUIToDismissKeyboard(mainView, R.id.ivAvatar);

        // Changing the weight of the view according to orientation.
        // This will make sure (hopefully) there is enough space to show the views in landscape mode.

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.linear).getLayoutParams();

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            layoutParams.weight = 3;
        }
        else {
            layoutParams.weight = 2;
        }
        mainView.findViewById(R.id.linear).setLayoutParams(layoutParams);

        etName = (EditText) mainView.findViewById(R.id.chat_sdk_et_name);
        etMail = (EditText) mainView.findViewById(R.id.chat_sdk_et_mail);
        etPhone = (EditText) mainView.findViewById(R.id.chat_sdk_et_phone_number);
    }

    @Override
    public void onResume() {
        super.onResume();

        //loadData();

        // Setting a listener to text change, The listener will take cate of indexing the bundle.
        TextWatcher emailTextWatcher = new SaveIndexDetailsTextWatcher(Keys.Email);
        TextWatcher nameTextWatcher= new SaveIndexDetailsTextWatcher(Keys.Name);
        TextWatcher phoneTextWatcher = new SaveIndexDetailsTextWatcher(Keys.Phone);

        etMail.addTextChangedListener(emailTextWatcher);
        etName.addTextChangedListener(nameTextWatcher);
        etPhone.addTextChangedListener(phoneTextWatcher);

    }

    @Override
    public void loadData() {
        super.loadData();
        setDetails();
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
    }

    @Override
    public void showSettings() {
        // Logout and return to the login activity.
//        FacebookManager.logout(getActivity());
//
//        NM.auth().logout();
//        uiHelper.startLoginActivity(true);
        UIHelper.shared().startEditProfileActivity(false, NM.currentUser());
    }

    /*############################################*/
    /* UI*/
    /** Fetching the user details from the user's metadata.*/
    private void setDetails () {

        User user = NM.currentUser();
        etName.setText(user.getName());
        etPhone.setText(user.metaStringForKey(Keys.Phone));
        etMail.setText(user.getEmail());

        progressBar.setVisibility(View.VISIBLE);

        UserAvatarHelper.loadAvatar(user, profileCircleImageView).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                profileCircleImageView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    /*############################################*/
}
