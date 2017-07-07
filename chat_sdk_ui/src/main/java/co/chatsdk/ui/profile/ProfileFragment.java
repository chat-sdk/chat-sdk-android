/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.profile;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.events.PredicateFactory;
import co.chatsdk.ui.R;

import com.braunster.chatsdk.network.FacebookManager;
import co.chatsdk.ui.helpers.SaveIndexDetailsTextWatcher;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by itzik on 6/17/2014.
 */
public class ProfileFragment extends AbstractProfileFragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private EditText etName, etMail, etPhone;
    private boolean imageLoading;

    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        NM.events().sourceOnMain().filter(PredicateFactory.type(EventType.UserMetaUpdated)).subscribe(new Consumer<NetworkEvent>() {
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

        setupTouchUIToDismissKeyboard(mainView, R.id.chat_sdk_circle_ing_profile_pic);

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
        TextWatcher emailTextWatcher = new SaveIndexDetailsTextWatcher(DaoDefines.Keys.Email);
        TextWatcher nameTextWatcher= new SaveIndexDetailsTextWatcher(DaoDefines.Keys.Name);
        TextWatcher phoneTextWatcher = new SaveIndexDetailsTextWatcher(DaoDefines.Keys.Phone);

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
    public void logout() {
        // Logout and return to the login activity.
        FacebookManager.logout(getActivity());

        NM.auth().logout();
        UIHelper.startLoginActivity(true);
    }

    /*############################################*/
    /* UI*/
    /** Fetching the user details from the user's metadata.*/
    private void setDetails(){

        BUser user = NM.currentUser();
        etName.setText(user.getName());
        etPhone.setText(user.metaStringForKey(DaoDefines.Keys.Phone));
        etMail.setText(user.getEmail());

        // Check to see if we can load the bitmap locally
        Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarURL());
        if(bitmap != null) {
            profileCircleImageView.setImageBitmap(bitmap);
            profileCircleImageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            // Load the remote image
            Ion.with(profileCircleImageView).placeholder(R.drawable.icn_32_profile_placeholder).load(user.getAvatarURL()).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    progressBar.setVisibility(View.INVISIBLE);
                    profileCircleImageView.setVisibility(View.VISIBLE);
                    imageLoading = false;
                }
            });
        }
    }


    /*############################################*/
}
