/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import android.text.TextUtils;

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

        path = path .replace("%3A", ":").replace("%253A", ":");
        
        // Cutting the the server path.
        if (BDefines.ServerUrl.length() < path.length())
        {
            this.path = path.substring(BDefines.ServerUrl.length());
        }

        components = this.path.split("/");

        for (int i = 0 ; i < components.length ; i+=2)
        {
            if (i+1 < components.length)
                tokens.put(components[i], components[i+1]);
            else
                tokens.put(components[i], null);

            keys.add(components[i]);
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
        return TextUtils.join("", keys);
    }

    public BPath addPathComponent(String component, String uid){
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
            String uid = tokens.get(key);
            if (uid != null)
                path = path.append(uid).append("/");
            else
                break;
        }


        // Remove the trailing slash and replacing the colon coding if necessary.
        return path.toString().substring(0, path.length() -1).replace("%3A", ":").replace("%253A", ":");
    }
}
