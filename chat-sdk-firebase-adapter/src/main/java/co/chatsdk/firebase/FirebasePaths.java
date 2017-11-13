/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package co.chatsdk.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public static DatabaseReference firebaseRawRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference firebaseRef () {
        return firebaseRawRef().child(ChatSDK.config().firebaseRootPath);
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

}
