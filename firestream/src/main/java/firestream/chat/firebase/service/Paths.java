package firestream.chat.firebase.service;

import firestream.chat.namespace.Fire;


public class Paths extends Keys {

    public static Path root() {
        return new Path(Fire.stream().getConfig().getRoot(), Fire.stream().getConfig().getSandbox());
    }

    public static Path usersPath() {
        return root().child(Users);
    }

    public static Path userPath(String uid) {
        return usersPath().child(uid);
    }

    public static Path userPath() {
        return userPath(currentUserId());
    }

    public static Path messagesPath(String uid) {
        return userPath(uid).child(Messages);
    }

    public static Path messagesPath() {
        return messagesPath(currentUserId());
    }

    public static Path userChatsPath() {
        return userPath(currentUserId()).child(Keys.Chats);
    }

    public static Path userMutedPath() {
        return userPath(currentUserId()).child(Keys.Muted);
    }

    public static Path userGroupChatPath(String chatId) {
        return userChatsPath().child(chatId);
    }

    public static Path messagePath(String messageId) {
        return messagePath(currentUserId(), messageId);
    }

    public static Path messagePath(String uid, String messageId) {
        return messagesPath(uid).child(messageId);
    }

    protected static String currentUserId() {
        return Fire.stream().currentUserId();
    }

    public static Path contactsPath() {
        return userPath().child(Contacts);
    }

    public static Path blockedPath() {
        return userPath().child(Blocked);
    }

    public static Path chatsPath() {
        return root().child(Chats);
    }

    public static Path chatPath(String chatId) {
        return chatsPath().child(chatId);
    }

    public static Path chatMetaPath(String chatId) {
        return chatsPath().child(chatId).child(Meta);
    }

    public static Path chatMessagesPath(String chatId) {
        return chatPath(chatId).child(Messages);
    }

    public static Path chatUsersPath(String chatId) {
        return chatPath(chatId).child(Users);
    }

}
