package com.braunster.chatsdk.firebase;

import com.firebase.client.Firebase;

/**
 * Created by itzik on 6/8/2014.
 */
public class FirebasePaths {

    public static final String FIREBASE_PATH = "https://incandescent-fire-3147.firebaseio.com/";

    private static Firebase firebaseRef;

    /* Get firebase ref and firbase ref for URL.
     * Get User ref for firebase id.
     * Get threads ref for firebase id.
     *
     * appendPathComponent? - assemble the path for given Strings i think.
     *
     * ASK the self keyword confuse the flow for me to understand.*/

    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/

    /** @return The main firebase ref.*/
    public static Firebase firebaseRef(){
        return firebaseRef = fb(FIREBASE_PATH);
    }

    /** @return Firebase object for give url.*/
    private static Firebase fb (String url){
        /* What the hell is initWithUrl stands for, Found the method in the BPath but not sure why and what.
        * It's a constructor https://www.firebase.com/docs/ios-api/Classes/Firebase.html#//api/name/initWithUrl:*/
        return new Firebase(url);
    }

    /** @return Firebase object for the base path of firebase + the component given..*/
    public static Firebase appendPathComponent(String component){
        /* Im pretty sure that this is what you wanted*/
        return fb(firebaseRef().getPath().toString() + "/" + component);
    }

    /* Users */

    /** @return The users main ref.*/
    public static Firebase userRef(){
        return appendPathComponent(FirebaseTags.BUsersPath);
    }

    /** @return The user ref for given id.*/
    public static Firebase userRef(String firebaseId){
        return userRef().child(firebaseId);
    }

    /* Threads */

    /** @return The thread main ref.*/
    public static Firebase threadRef(){
        return appendPathComponent(FirebaseTags.BThreadPath);
    }
    /** @return The thread ref for given id.*/
    public static Firebase threadRef(String firebaseId){
        return threadRef().child(firebaseId);
    }
    /** @return The public threads ref .*/
    public static Firebase publicThreadRef(){
        return appendPathComponent(FirebaseTags.BPublicThreadPath);
    }
}
