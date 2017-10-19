/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package co.chatsdk.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.session.ChatSDK;

public class FirebasePaths{

    public static final String UsersPath = "users";
    public static final String MessagesPath = "messages";
    public static final String ThreadsPath = "threads";
    public static final String PublicThreadsPath = "public-threads";
    public static final String DetailsPath = "details";
    public static final String IndexPath = "searchIndex";
    public static final String OnlinePath = "online";
    public static final String MetaPath = "meta";
    public static final String FollowersPath = "followers";
    public static final String FollowingPath = "follows";
    public static final String Image = "imaeg";
    public static final String Thumbnail = "thumbnail";
    public static final String UpdatedPath = "updated";
    public static final String LastMessagePath = "lastMessage";
    public static final String TypingPath = "typing";
    public static final String ReadPath = "read";

    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/
    /** @return The main databse ref.*/

    public static DatabaseReference firebaseRef () {
        String path = ChatSDK.config().fullFirebasePath();
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("Please set the server url in Keys class");
        }

        return fb(path);
    }

    /** @return Firebase object for give url.*/
    private static DatabaseReference fb (String url) {
        if(!url.substring(url.length() - 1).equals('/')) {
            url += "/";
        }
        return FirebaseDatabase.getInstance().getReferenceFromUrl(url);
    }

    /* Users */
    /** @return The users main ref.*/
    public static DatabaseReference usersRef(){
        return firebaseRef().child(UsersPath);
    }

    /** @return The user ref for given id.*/
    public static DatabaseReference userRef(String firebaseId){
        return usersRef().child(firebaseId);
    }

    /** @return The user threads ref.*/
    public static DatabaseReference userThreadsRef(String firebaseId){
        return usersRef().child(firebaseId).child(ThreadsPath);
    }

    /** @return The user meta ref for given id.*/
    public static DatabaseReference userMetaRef(String firebaseId){
        return usersRef().child(firebaseId).child(MetaPath);
    }

    public static DatabaseReference userOnlineRef(String firebaseId){
        return userRef(firebaseId).child(OnlinePath);
    }

    public static DatabaseReference userFollowingRef(String firebaseId){
        return userRef(firebaseId).child(FollowingPath);
    }

    public static DatabaseReference userFollowersRef(String firebaseId){
        return userRef(firebaseId).child(FollowersPath);
    }

    /* Threads */
    /** @return The thread main ref.*/
    public static DatabaseReference threadRef(){
        return firebaseRef().child(ThreadsPath);
    }

    /** @return The thread ref for given id.*/
    public static DatabaseReference threadRef(String firebaseId){
        return threadRef().child(firebaseId);
    }

    public static DatabaseReference threadUsersRef(String firebaseId){
        return threadRef().child(firebaseId).child(UsersPath);
    }

    public static DatabaseReference threadDetailsRef(String firebaseId){
        return threadRef().child(firebaseId).child(DetailsPath);
    }

    public static DatabaseReference threadMessagesRef(String firebaseId){
        return threadRef(firebaseId).child(MessagesPath);
    }
    
    public static DatabaseReference publicThreadsRef(){
        return firebaseRef().child(PublicThreadsPath);
    }

    public static DatabaseReference onlineRef(String userEntityID){
        return firebaseRef().child(OnlinePath).child(userEntityID);
    }


    /* Index */
    public static DatabaseReference indexRef(){
        return firebaseRef().child(IndexPath);
    }

    @Deprecated
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

    public static PathBuilder userThreadsPath (String userID, String threadID) {
        return new PathBuilder(Keys.Users)
                .append(userID)
                .append(Keys.Threads)
                .append(threadID);

    }

    public static PathBuilder threadUsersPath (String threadID, String userID) {
        return new PathBuilder(Keys.Threads)
                .append(threadID)
                .append(Keys.Users)
                .append(userID);

    }

//    public static int providerToInt(String provider){
//        if (provider.equals(Keys.ProviderString.Password))
//        {
//            return Keys.ProviderInt.Password;
//        }
//        else if (provider.equals(Keys.ProviderString.Facebook))
//        {
//            return Keys.ProviderInt.Facebook;
//        }
//        else if (provider.equals(Keys.ProviderString.Google))
//        {
//            return Keys.ProviderInt.Google;
//        }
//        else if (provider.equals(Keys.ProviderString.Twitter))
//        {
//            return Keys.ProviderInt.Twitter;
//        }
//        else if (provider.equals(Keys.ProviderString.Anonymous))
//        {
//            return Keys.ProviderInt.Anonymous;
//        }
//        else if (provider.equals(Keys.ProviderString.Custom))
//        {
//            return Keys.ProviderInt.Custom;
//        }
//
//        throw new IllegalArgumentException("No provider was found matching requested. Provider: " + provider);
//    }

//    public static String providerToString(int provider){
//
//        switch (provider){
//            case Keys.ProviderInt.Password:
//                return Keys.ProviderString.Password;
//            case Keys.ProviderInt.Facebook:
//                return Keys.ProviderString.Facebook;
//            case Keys.ProviderInt.Google:
//                return Keys.ProviderString.Google;
//            case Keys.ProviderInt.Twitter:
//                return Keys.ProviderString.Twitter;
//            case Keys.ProviderInt.Anonymous:
//                return Keys.ProviderString.Anonymous;
//            case Keys.ProviderInt.Custom:
//                return Keys.ProviderString.Custom;
//
//            default:
//                /*return ProviderString.Password;*/
//                throw new IllegalArgumentException("Np provider was found matching requested.");
//        }
//    }
}
