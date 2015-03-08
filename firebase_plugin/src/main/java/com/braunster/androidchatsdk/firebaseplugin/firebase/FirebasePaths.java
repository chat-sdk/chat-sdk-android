package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.firebase.client.Firebase;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.braunster.chatsdk.network.BDefines.ServerUrl;

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
