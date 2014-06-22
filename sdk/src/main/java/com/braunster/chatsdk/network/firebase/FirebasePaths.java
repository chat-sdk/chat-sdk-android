package com.braunster.chatsdk.network.firebase;

import com.firebase.client.Firebase;

/**
 * Created by itzik on 6/8/2014.
 */
public class FirebasePaths extends Firebase{

    public static final String FIREBASE_PATH = "https://incandescent-fire-3147.firebaseio.com/";

    private  FirebasePaths firebaseRef;

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
        return fb(this.toString() + "/" + component);
    }

    /* Users */
    /** @return The users main ref.*/
    public FirebasePaths userRef(){
        return firebaseRef().appendPathComponent(FirebaseTags.BUsersPath);
    }
    /** @return The user ref for given id.*/
    public Firebase userRef(String firebaseId){
        return userRef().child(firebaseId);
    }

    /* Threads */
    /** @return The thread main ref.*/
    public Firebase threadRef(){
        return appendPathComponent(FirebaseTags.BThreadPath);
    }
    /** @return The thread ref for given id.*/
    public Firebase threadRef(String firebaseId){
        return threadRef().child(firebaseId);
    }
    /** @return The public threads ref .*/
    public Firebase publicThreadRef(){
        return appendPathComponent(FirebaseTags.BPublicThreadPath);
    }
}
