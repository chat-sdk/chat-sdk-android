package sdk.chat.micro.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.message.Message;

public class Paths extends Keys {

    public static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

    public static DocumentReference userRef (String uid) {
        return db().collection(Users).document(uid);
    }

    public static DocumentReference userRef () {
        return userRef(currentUid());
    }

    public static CollectionReference messagesRef (String uid) {
        return userRef(uid).collection(Messages);
    }

    public static CollectionReference messagesRef () {
        return messagesRef(currentUid());
    }

    public static CollectionReference userGroupChatsRef () {
        return userRef(currentUid()).collection(Keys.GroupChats);
    }

    public static DocumentReference messageRef (String messageId) {
        return messageRef(currentUid(), messageId);
    }

    public static DocumentReference messageRef (String uid, String messageId) {
        return messagesRef(uid).document(messageId);
    }

    protected static String currentUid () {
        return MicroChatSDK.shared().currentUserId();
    }

    public static CollectionReference contactsRef() {
        return userRef().collection(Contacts);
    }

    public static CollectionReference blockedRef() {
        return userRef().collection(Blocked);
    }

    public static CollectionReference groupChatsRef() {
        return db().collection(GroupChats);
    }

    public static DocumentReference groupChatRef(String chatId) {
        return groupChatsRef().document(chatId);
    }

    public static CollectionReference groupChatMessagesRef(String chatId) {
        return groupChatRef(chatId).collection(Messages);
    }

    public static CollectionReference groupChatUsersRef(String chatId) {
        return groupChatRef(chatId).collection(Users);
    }

    public static CollectionReference groupChatMetaRef(String chatId) {
        return groupChatRef(chatId).collection(Meta);
    }

}
