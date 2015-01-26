package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.firebase.client.Firebase;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 */
public class FirebasePaths extends Firebase{

    private StringBuilder builder = new StringBuilder();

    private FirebasePaths(String url) {
        super(url);
    }

    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/
    /** @return The main firebase ref.*/
    public static FirebasePaths firebaseRef(){
        if (StringUtils.isBlank(BDefines.ServerUrl))
            throw new NullPointerException("Please set the server url in BDefines class");

        return fb(BDefines.ServerUrl);
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


    public static final class ProviderString{
        public static final String Anonymous = "anonymous";
        public static final String Password = "password";
        public static final String Facebook = "facebook";
        public static final String Twitter = "twitter";
        public static final String Google = "google";
    }

    public static final class ProviderInt{
        public static final int Password = 1;
        public static final int Facebook = 2;
        public static final int Google = 3;
        public static final int Twitter = 4;
        public static final int Anonymous = 5;
    }

    public static int providerToInt(String provider){
        if (provider.equals(ProviderString.Password))
        {
            return ProviderInt.Password;
        }
        else if (provider.equals(ProviderString.Facebook))
        {
            return ProviderInt.Facebook;
        }
        else if (provider.equals(ProviderString.Google))
        {
            return ProviderInt.Google;
        }
        else if (provider.equals(ProviderString.Twitter))
        {
            return ProviderInt.Twitter;
        }
        else if (provider.equals(ProviderString.Anonymous))
        {
            return ProviderInt.Anonymous;
        }

        throw new IllegalArgumentException("Np provider was found matching requested.");
    }

    public static String providerToString(int provider){

        switch (provider){
            case ProviderInt.Password:
                return ProviderString.Password;
            case ProviderInt.Facebook:
                return ProviderString.Facebook;
            case ProviderInt.Google:
                return ProviderString.Google;
            case ProviderInt.Twitter:
                return ProviderString.Twitter;
            case ProviderInt.Anonymous:
                return ProviderString.Anonymous;

            default:
                /*return ProviderString.Password;*/
                throw new IllegalArgumentException("Np provider was found matching requested.");
        }
    }
}
