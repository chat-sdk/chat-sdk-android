package com.braunster.chatsdk.network.firebase;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 */
public class FirebasePaths extends Firebase{

    public static final String FIREBASE_PATH = "https://incandescent-fire-3147.firebaseio.com/";
//    public static final String FIREBASE_PATH = "https://chat-sdk-android-2.firebaseio.com/";
//public static final String FIREBASE_PATH = "https://chatsdkbrett.firebaseio.com/";

    private StringBuilder builder = new StringBuilder();

    private FirebasePaths(String url) {
        super(url);
    }

    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/
    /** @return The main firebase ref.*/
    public static FirebasePaths firebaseRef(){
        return fb(FIREBASE_PATH);
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
        builder = new StringBuilder(this.toString()).append("/").append(component);
        return fb(builder.toString());
    }

    /* Users */
    /** @return The users main ref.*/
    public static FirebasePaths userRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BUsersPath);
    }
    /** @return The user ref for given id.*/
    public static FirebasePaths userRef(String firebaseId){
        return userRef().appendPathComponent(firebaseId);
    }

    /* Threads */
    /** @return The thread main ref.*/
    public static FirebasePaths threadRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BThreadPath);
    }
    /** @return The thread ref for given id.*/
    public static FirebasePaths threadRef(String firebaseId){
        return threadRef().appendPathComponent(firebaseId);
    }

    public static FirebasePaths publicThreadsRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BPublicThreadPath);
    }

    /** @return The public threads ref .*/
    public Firebase publicThreadRef(){
        return appendPathComponent(BFirebaseDefines.Path.BPublicThreadPath);
    }

    public static FirebasePaths indexRef(){
        return firebaseRef().appendPathComponent(BFirebaseDefines.Path.BIndexPath);
    }

    public static FirebasePaths userOnlineRef(String firebaseId){
        return userRef(firebaseId).appendPathComponent(BFirebaseDefines.Path.BOnlinePath);
    }

    public static FirebasePaths userFollowsRef(String firebaseId){
        return userRef(firebaseId).appendPathComponent(BFirebaseDefines.Path.BFollows);
    }

    public static FirebasePaths userFollowersRef(String firebaseId){
        return userRef(firebaseId).appendPathComponent(BFirebaseDefines.Path.BFollowers);
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
}
