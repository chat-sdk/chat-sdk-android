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

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.utils.volley.VolleyUtils;
import com.bugsense.trace.BugSenseHandler;

import org.apache.commons.lang3.StringUtils;

import tk.wanderingdevelopment.chatsdk.core.interfaces.UiLauncherInterface;

/**
 * Created by itzik on 6/8/2014.
 */
@Deprecated
public class BNetworkManager {

    private static final String CHAT_SDK_SHRED_PREFS = "ChatSDK_Prefs";
    public static final boolean BUGSENSE_ENABLED = false;
    public static final boolean PushEnabledDefaultValue = true;

    public static SharedPreferences preferences;

    private static BNetworkManager instance;

    private static UiLauncherInterface uiLauncherInterface;

    private static Context context;

    public static void init(Context ctx){
        context = ctx.getApplicationContext();

        preferences = context.getSharedPreferences(CHAT_SDK_SHRED_PREFS, Context.MODE_PRIVATE);
        VolleyUtils.init(context);
        DaoCore.init(context);

        //Bug Sense
        if (BNetworkManager.BUGSENSE_ENABLED && StringUtils.isNotEmpty( context.getString(R.string.bug_sense_key) )) {
            BugSenseHandler.initAndStartSession(context, context.getString(R.string.bug_sense_key));
            BugSenseHandler.addCrashExtraData("Version", BuildConfig.VERSION_NAME);
        }
    }

    public static BNetworkManager sharedManager(){
        if (instance == null) {
            instance = new BNetworkManager();
        }
        return instance;
    }

    public static void setUiLauncherInterface(UiLauncherInterface uiLauncherInterface) {
        BNetworkManager.uiLauncherInterface = uiLauncherInterface;
    }

    public static UiLauncherInterface getUiLauncherInterface() {
        return uiLauncherInterface;
    }


//    /** Always safe to call*/
//    public static SharedPreferences getUserPrefs(String entityId){
//        return context.getSharedPreferences(entityId, Context.MODE_PRIVATE);
//    }
//    /** Safe to call after login.*/
//    public static SharedPreferences getCurrentUserPrefs(){
//        return context.getSharedPreferences(NM.currentUser().getEntityID(), Context.MODE_PRIVATE);
//    }
}
