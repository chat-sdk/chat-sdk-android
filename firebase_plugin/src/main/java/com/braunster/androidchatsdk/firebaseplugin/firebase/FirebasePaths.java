/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.firebase.client.Firebase;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.braunster.chatsdk.network.BDefines.ServerUrl;

public class FirebasePaths extends Firebase{

    private StringBuilder builder = new StringBuilder();

    private FirebasePaths(String url) {
        super(url);
    }

    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/
    /** @return The main firebase ref.*/
    public static FirebasePaths firebaseRef(){
        if (StringUtils.isBlank(ServerUrl))
            throw new NullPointerException("Please set the server url in BDefines class");

        return fb(ServerUrl);
    }

    /** @return Firebase object for give url.*/
    private static FirebasePaths fb (String url){
        /* What the hell is initWithUrl stands for, Found the method in the BPath but not sure why and what.
        * It's a constructor https://www.firebase.com/docs/ios-api/Classes/Firebase.html#//api/name/initWithUrl:*/
        return new FirebasePaths(url);
    }
    /** @return Firebase object for the base path of firebase + the component given..*/
    public FirebasePaths appendPathComponent(String component){
        /* Im pretty sure that this is what you wanted*/
        builder.setLength(0);
        builder.append(this.toString()).append("/").append(component);
        return fb(builder.toString().replace("%3A", ":").replace("%253A", ":"));
    }

    /* Users */
    /** @return The users main ref.*/
    public static Firebase userRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BUsers);
    }
   
    /** @return The user ref for given id.*/
    public static Firebase userRef(String firebaseId){
        return userRef().child(firebaseId);
    }

    /** @return The user meta ref for given id.*/
    public static Firebase userMetaRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BMeta);
    }

    public static Firebase userOnlineRef(){
        return firebaseRef().child(BFirebaseDefines.Path.BOnline);
    }

    public static Firebase userOnlineRef(String firebaseId){
        return userOnlineRef().child(firebaseId);
    }

    public static Firebase userFollowsRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BFollows);
    }

    public static Firebase userFollowersRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BFollowers);
    }

    public static Firebase userFollowsRef(String firebaseId, String uid){
        return userFollowsRef(firebaseId).child(uid);
    }

    public static Firebase userFollowersRef(String firebaseId, String uid){
        return userFollowersRef(firebaseId).child(uid);
    }

    public static Firebase userThreadRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BThread);
    }

    public static Firebase userThreadRef(String firebaseId, String tid){
        return userThreadRef(firebaseId).child(tid);
    }

    public static Firebase userFriendsRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BFriends);
    }

    public static Firebase userFriendsRef(String firebaseId, String uid){
        return userFriendsRef(firebaseId).child(uid);
    }

    public static Firebase userBlockedRef(String firebaseId){
        return userRef(firebaseId).child(BFirebaseDefines.Path.BBlocked);
    }

    public static Firebase userBlockedRef(String firebaseId, String uid){
        return userBlockedRef(firebaseId).child(uid);
    }


    /* Online Users */
    public static Firebase onlineUsersRef(String firebaseId){
        return firebaseRef().child(BFirebaseDefines.Path.BOnline);
    }

    public static Firebase onlineUsersRef(String firebaseId, String uid){
        return onlineUsersRef(firebaseId).child(uid);
    }

    /* Threads */
    /** @return The thread main ref.*/
    public static Firebase threadRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BThread);
    }

    /** @return The thread ref for given id.*/
    public static Firebase threadRef(String firebaseId){
        return threadRef().child(firebaseId);
    }

    public static Firebase threadLastMessageRef(String firebaseId){
        return threadRef(firebaseId)
                .child(BFirebaseDefines.Path.BLastMessage);
    }

    public static Firebase threadMessagesRef(String firebaseId){
        return threadRef(firebaseId).child(BFirebaseDefines.Path.BMessages);
    }

    public static Firebase threadMetaRef(String firebaseId){
        return threadRef(firebaseId).child(BFirebaseDefines.Path.BMeta);
    }

    public static Firebase threadTypingRef(String firebaseId, String uid){
        return threadRef(firebaseId).child(BFirebaseDefines.Path.BTyping).child(uid);
    }

    public static Firebase threadUserRef(String firebaseId){
        return threadRef(firebaseId).child(BFirebaseDefines.Path.BUsersMeta);
    }

    public static Firebase threadUserRef(String firebaseId, String uid){
        return threadUserRef(firebaseId).child(uid);
    }



    public static Firebase publicThreadsRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BPublicThread);
    }

    public static Firebase publicThreadRef(String firebaseId){
        return publicThreadsRef().child(firebaseId);
    }

    /* Index */
    public static Firebase searchIndexRef(){
        return firebaseRef().child(BFirebaseDefines.Path.BIndex);
    }

    public static Firebase searchIndexRef(String firebaseId){
        return searchIndexRef().child(firebaseId);
    }


    /* State */
    public static Firebase entityRef(String path, String entityId){
        return firebaseRef().child(path).child(entityId);
    }

    public static Firebase entityStateRef(String path, String entityId,String key){
        return entityRef(path, entityId).child(BFirebaseDefines.Path.BState).child(key);
    }

    public static Firebase entityKeyRef(String path, String entityId,String key){
        return entityRef(path, entityId).child(key);
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

        throw new IllegalArgumentException("Np provider was found matching requested.");
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
