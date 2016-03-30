/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.BuildConfig;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractLoginActivity;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatSDKLoginActivity extends ChatSDKAbstractLoginActivity implements View.OnClickListener{

    private static final String TAG = ChatSDKLoginActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.LoginActivity;

    private Button btnLogin, btnReg, btnAnon, btnTwitter;
    private ImageView appIconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFacebookIntegration(getNetworkAdapter().facebookEnabled());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activty_login);

        setExitOnBackPressed(true);

        View view = findViewById(R.id.chat_sdk_root_view);
        
        setupTouchUIToDismissKeyboard(view);

        initViews();

        ((TextView) findViewById(R.id.chat_sdk_txt_version)).setText(String.valueOf(BuildConfig.VERSION_NAME));
    }

    @Override
    protected void initViews(){
        super.initViews();
        facebookLogin.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        facebookLogin.setBackgroundResource(R.drawable.ic_facebook);

        if (integratedWithFacebook)
        {

            facebookLogin.setReadPermissions(Arrays.asList("email", "user_friends"));
        }

        btnLogin = (Button) findViewById(R.id.chat_sdk_btn_login);
        btnAnon = (Button) findViewById(R.id.chat_sdk_btn_anon_login);
        btnTwitter = (Button) findViewById(R.id.chat_sdk_btn_twitter_login);
        btnReg = (Button) findViewById(R.id.chat_sdk_btn_register);
        etEmail = (EditText) findViewById(R.id.chat_sdk_et_mail);
        etPass = (EditText) findViewById(R.id.chat_sdk_et_password);

        appIconImage = (ImageView) findViewById(R.id.app_icon);

        appIconImage.post(new Runnable() {
            @Override
            public void run() {
                appIconImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initListeners(){
        /* Registering listeners.*/
        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnAnon.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);

        etPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    btnLogin.callOnClick();
                }
                return false;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        initListeners();
    }

    /* Dismiss dialog and open main activity.*/
    @Override
    protected void afterLogin(){
        super.afterLogin();

        // Updating the version name.
        BUser curUser = getNetworkAdapter().currentUserModel();
        
        String version = BDefines.BAppVersion,
                metaVersion = curUser.metaStringForKey(BDefines.Keys.BVersion);
        
        if (StringUtils.isNotEmpty(version))
        {
            if (StringUtils.isEmpty(metaVersion) || !metaVersion.equals(version))
            {
                curUser.setMetadataString(BDefines.Keys.BVersion, version);
            }

            DaoCore.updateEntity(curUser);
        }
        
        startMainActivity();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.chat_sdk_btn_login) {
            passwordLogin();
        }
        else if (i == R.id.chat_sdk_btn_anon_login) {
            anonymosLogin();
        }
        else if (i == R.id.chat_sdk_btn_register)
        {
            register();
        }
        else if (i == R.id.chat_sdk_btn_twitter_login){
            twitterLogin();
        }
    }

}
