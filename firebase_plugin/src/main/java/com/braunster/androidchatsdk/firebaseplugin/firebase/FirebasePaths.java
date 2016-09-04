/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.braunster.chatsdk.network.BDefines.ServerUrl;

public class FirebasePaths{

    private String url;
    private static StringBuilder builder = new StringBuilder();

    private FirebasePaths(String url) {
        this.url = url;
    }

    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/
    /** @return The main databse ref.*/
    public static DatabaseReference firebaseRef(){
        if (StringUtils.isBlank(ServerUrl))
            throw new NullPointerException("Please set the server url in BDefines class");

        return fb(ServerUrl);
    }

    /** @return Firebase object for give url.*/
    private static DatabaseReference fb (String url){
        return FirebaseDatabase.getInstance().getReferenceFromUrl(url);
    }

    /* Users */
    /** @return The users main ref.*/
    public static DatabaseReference userRef(){
        return firebaseRef().child(BFirebaseDefines.Path.BUsersPath);
    }
   
    /** @return The user ref for given id.*/
    public static DatabaseReference userRef(String firebaseId){
        return userRef().child(firebaseId);
    }

    /** @return The user meta ref for given id.*/
    public static DatabaseReference userMetaRef(String firebaseId){
        DatabaseReference userMetaRef = userRef().child(firebaseId);
        return userMetaRef.child(BFirebaseDefines.Path.BMetaPath);
    }

    public static DatabaseReference userOnlineRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BOnlinePath);
    }

    public static DatabaseReference userFollowsRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BFollows);
    }

    public static DatabaseReference userFollowersRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.FollowerLinks);
    }

    /* Threads */
    /** @return The thread main ref.*/
    public static DatabaseReference threadRef(){
        return firebaseRef().child(BFirebaseDefines.Path.BThreadPath);
    }

    /** @return The thread ref for given id.*/
    public static DatabaseReference threadRef(String firebaseId){
        return threadRef().child(firebaseId);
    }

    public static DatabaseReference threadMessagesRef(String firebaseId){
        return threadRef(firebaseId).child(BFirebaseDefines.Path.BMessagesPath);
    }
    
    public static DatabaseReference publicThreadsRef(){
        return firebaseRef().child(BFirebaseDefines.Path.BPublicThreadPath);
    }

    /* Index */
    public static DatabaseReference indexRef(){
        return firebaseRef().child(BFirebaseDefines.Path.BIndexPath);
    }

    public static Map<String, Object> getMap(String[] keys,  Object...values){
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0 ; i < keys.length; i++){

            // More values then keys entered.
            if (i == values.length)
                break;

            map.put(keys[i], values[i]);
        }

        return map;
    }




    public static int providerToInt(String provider){
        if (provider.equals(BDefines.ProviderString.Password))
        {
            return BDefines.ProviderInt.Password;
        }
        else if (provider.equals(BDefines.ProviderString.Facebook))
        {
            return BDefines.ProviderInt.Facebook;
        }
        else if (provider.equals(BDefines.ProviderString.Google))
        {
            return BDefines.ProviderInt.Google;
        }
        else if (provider.equals(BDefines.ProviderString.Twitter))
        {
            return BDefines.ProviderInt.Twitter;
        }
        else if (provider.equals(BDefines.ProviderString.Anonymous))
        {
            return BDefines.ProviderInt.Anonymous;
        }
        else if (provider.equals(BDefines.ProviderString.Custom))
        {
            return BDefines.ProviderInt.Custom;
        }

        throw new IllegalArgumentException("No provider was found matching requested. Provider: " + provider);
    }

    public static String providerToString(int provider){

        switch (provider){
            case BDefines.ProviderInt.Password:
                return BDefines.ProviderString.Password;
            case BDefines.ProviderInt.Facebook:
                return BDefines.ProviderString.Facebook;
            case BDefines.ProviderInt.Google:
                return BDefines.ProviderString.Google;
            case BDefines.ProviderInt.Twitter:
                return BDefines.ProviderString.Twitter;
            case BDefines.ProviderInt.Anonymous:
                return BDefines.ProviderString.Anonymous;
            case BDefines.ProviderInt.Custom:
                return BDefines.ProviderString.Custom;

            default:
                /*return ProviderString.Password;*/
                throw new IllegalArgumentException("Np provider was found matching requested.");
        }
    }
}
