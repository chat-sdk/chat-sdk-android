package com.braunster.chatsdk.network.firebase;

import android.text.TextUtils;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 */
public class BPath {

    private static final String TAG = BPath.class.getSimpleName();
    private static final boolean DEBUG = Debug.BPath;

    private Map<String, String> tokens = new HashMap<String, String>();
    private ArrayList<String> keys = new ArrayList<String>();
    private String [] components;
    private String path = "";

    public BPath(){

    }

    public BPath(String path){
        if (DEBUG) Log.d(TAG, "Init path, Path: " + path);

        // Cutting the the server path.
        if (FirebasePaths.FIREBASE_PATH.length() < path.length())
        {
            this.path = path.substring(FirebasePaths.FIREBASE_PATH.length());
            if (DEBUG) Log.d(TAG, "Path after cut: " + this.path);
        }

        components = this.path.split("/");

        for (int i = 0 ; i < components.length ; i+=2)
        {
            if (i+1 < components.length)
                tokens.put(components[i], components[i+1]);
            else
                tokens.put(components[i], null);

            keys.add(components[i]);

            if (DEBUG) Log.d(TAG, "Token: " + components[i] + ", Key: "
                    + tokens.get( (components[i] == null ? "Null" : components[i]) ));
        }
    }

    public static BPath pathWithPath(String path){
        return new BPath(path);
    }

    public String idForIndex(int index){
        if (index < keys.size())
        {
            if (tokens.containsKey(keys.get(index)))
                return tokens.get(keys.get(index));
        }

        return null;
    }

    public boolean isEqualToComponent(String...components){
        return this.getObjectIdentifier().equals(concatStrings(components));
    }

    private String concatStrings(String[] strings){
        String concat = "";
        for (String s : strings)
            concat = concat.concat(s);
        return concat;
    }

    private String getObjectIdentifier(){
        if (DEBUG) Log.d(TAG, "getObjectIdentifier, Result: " + TextUtils.join("", keys));
        return TextUtils.join("", keys);
    }

    public BPath addPathComponent(String component, String uid){
        if (DEBUG) Log.v(TAG, "addPathComponent, Component: " + component + ", UID: " + uid);
        keys.add(component);

        if (uid == null || uid.replace(" ", "").length() == 0){
            uid = null;
        }

        tokens.put(component, uid);

        return this;
    }

    public String getPath() {
        StringBuilder path = new StringBuilder();

        for (String key : keys)
        {
            path = path.append(key).append("/");
            if (DEBUG)  Log.v(TAG, "Key: " + key);
            String uid = tokens.get(key);
            if (uid != null)
                path = path.append(uid).append("/");
            else
                break;
        }

        if (DEBUG) Log.i(TAG, "getPath, Result: " + path.toString());

        // Remove the trailing slash
        return path.toString().substring(0, path.length() -1);
    }
}
