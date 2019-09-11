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
    public static final String ContactsPath = "contacts";
    public static final String PublicThreadsPath = "public-threads";
    public static final String DetailsPath = "details";
    public static final String OnlinePath = "online";
    public static final String MetaPath = "meta";
    public static final String UpdatedPath = "updated";
    public static final String LastMessagePath = "lastMessage";
    public static final String TypingPath = "typing";
    public static final String ReadPath = Keys.Read;
    public static final String LocationPath = "location";


    /* Not sure if this the wanted implementation but its give the same result as the objective-C code.*/
    /** @return The main databse ref.*/

    public static DatabaseReference firebaseRawRef() {
        return FirebaseCoreHandler.database().getReference();
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

    /** @return The user threads ref.*/
    public static DatabaseReference userContactsRef(String firebaseId){
        return usersRef().child(firebaseId).child(ContactsPath);
    }

    /** @return The user meta ref for given id.*/
    public static DatabaseReference userMetaRef(String firebaseId){
        return usersRef().child(firebaseId).child(MetaPath);
    }

    public static DatabaseReference userOnlineRef(String firebaseId){
        return userRef(firebaseId).child(OnlinePath);
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

    public static DatabaseReference threadLastMessageRef(String firebaseId){
        return threadRef().child(firebaseId).child(LastMessagePath);
    }

    public static DatabaseReference threadMessagesRef(String firebaseId){
        return threadRef(firebaseId).child(MessagesPath);
    }

    public static DatabaseReference threadMessageRef(String threadID, String messageID){
        return threadMessagesRef(threadID).child(messageID);
    }

    public static DatabaseReference threadMessagesReadRef(String threadID, String messageID){
        return threadMessageRef(threadID, messageID).child(ReadPath);
    }

    public static DatabaseReference threadMetaRef(String firebaseId){
        return threadRef(firebaseId).child(MetaPath);
    }

    public static DatabaseReference publicThreadsRef(){
        return firebaseRef().child(PublicThreadsPath);
    }

    public static DatabaseReference onlineRef(String userEntityID){
        return firebaseRef().child(OnlinePath).child(userEntityID);
    }

}
