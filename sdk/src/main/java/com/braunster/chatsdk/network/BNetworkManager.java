package com.braunster.chatsdk.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.ActivityListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public class BNetworkManager {

    private static final String TAG = BNetworkManager.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static SharedPreferences preferences;

    private static BNetworkManager instance;

    private HashSet<ActivityListener> listeners = new HashSet<ActivityListener>();

    private List<ActivityListener> activityListeners= new ArrayList<ActivityListener>();

    private AbstractNetworkAdapter networkAdapter;

    public static void init(Context ctx){

        preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        VolleyUtills.init(ctx);
        DaoCore.init(ctx);
    }

    public static BNetworkManager sharedManager(){
//        if (DEBUG) Log.v(TAG, "sharedManager");
        if (instance == null) {
            instance = new BNetworkManager();
        }
        return instance;
    }

    public void setNetworkAdapter(AbstractNetworkAdapter networkAdapter) {
        this.networkAdapter = networkAdapter;
    }

    public AbstractNetworkAdapter getNetworkAdapter() {
        return networkAdapter;
    }
}
