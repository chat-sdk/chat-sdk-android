/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;


import android.content.Context;
import android.content.SharedPreferences;

import com.braunster.chatsdk.BuildConfig;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.bugsense.trace.BugSenseHandler;

import org.apache.commons.lang3.StringUtils;

import tk.wanderingdevelopment.chatsdk.core.interfaces.AuthInterface;
import tk.wanderingdevelopment.chatsdk.core.interfaces.CoreInterface;
import tk.wanderingdevelopment.chatsdk.core.interfaces.ThreadsInterface;
import tk.wanderingdevelopment.chatsdk.core.interfaces.UiLauncherInterface;

/**
 * Created by itzik on 6/8/2014.
 */
public class BNetworkManager {

    private static final String TAG = BNetworkManager.class.getSimpleName();
    private static final boolean DEBUG = Debug.BNetworkManager;

    private static final String CHAT_SDK_SHRED_PREFS = "ChatSDK_Prefs";
    public static final boolean BUGSENSE_ENABLED = false, PushEnabledDefaultValue = true;

    public static SharedPreferences preferences;

    private static BNetworkManager instance;

    private static AuthInterface authInterface;
    private static CoreInterface coreInterface;
    private static ThreadsInterface threadsInterface;
    private static UiLauncherInterface uiLauncherInterface;


    private static Context context;

    public static void init(Context ctx){
        context = ctx.getApplicationContext();

        preferences = context.getSharedPreferences(CHAT_SDK_SHRED_PREFS, Context.MODE_PRIVATE);
        VolleyUtils.init(context);
        DaoCore.init(context);

        BFacebookManager.init(context.getString(R.string.facebook_id), context);

        
        //Bug Sense
        if (BNetworkManager.BUGSENSE_ENABLED && StringUtils.isNotEmpty( context.getString(R.string.bug_sense_key) )) {
            BugSenseHandler.initAndStartSession(context, context.getString(R.string.bug_sense_key));
            BugSenseHandler.addCrashExtraData("Version", BuildConfig.VERSION_NAME);
        }
    }

    public static Context getAppContext() {
        return context;
    }

    public static BNetworkManager sharedManager(){
        if (instance == null) {
            instance = new BNetworkManager();
        }
        return instance;
    }

    public static void setAuthInterface(AuthInterface authInterface) {
        BNetworkManager.authInterface = authInterface;
    }

    public static AuthInterface getAuthInterface() {
        return authInterface;
    }

    public static void setUiLauncherInterface(UiLauncherInterface uiLauncherInterface) {
        BNetworkManager.uiLauncherInterface = uiLauncherInterface;
    }

    public static UiLauncherInterface getUiLauncherInterface() {
        return uiLauncherInterface;
    }

    public static void setThreadsInterface(ThreadsInterface threadsInterface) {
        BNetworkManager.threadsInterface = threadsInterface;
    }

    public static ThreadsInterface getThreadsInterface() {
        return threadsInterface;
    }

    public static void setCoreInterface(CoreInterface coreInterface) {
        BNetworkManager.coreInterface = coreInterface;
    }

    public static CoreInterface getCoreInterface() {
        return coreInterface;
    }

    /** Always safe to call*/
    public static SharedPreferences getUserPrefs(String entityId){
        return context.getSharedPreferences(entityId, Context.MODE_PRIVATE);
    }
    /** Safe to call after login.*/
    public static SharedPreferences getCurrentUserPrefs(){
        return context.getSharedPreferences(BNetworkManager.getCoreInterface().currentUserModel().getEntityID(), Context.MODE_PRIVATE);
    }
}
