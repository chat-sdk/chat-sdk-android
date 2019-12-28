package firefly.sdk.chat.firebase.service;

import firefly.sdk.chat.namespace.Fl;

public class Paths extends Keys {

    public static Path root() {
        return new Path(Fl.y.getConfig().root, Fl.y.getConfig().sandbox);
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

    public static Path userGroupChatsPath() {
        return userPath(currentUserId()).child(Keys.Chats);
    }

    public static Path userGroupChatPath(String chatId) {
        return userGroupChatsPath().child(chatId);
    }

    public static Path messagePath(String messageId) {
        return messagePath(currentUserId(), messageId);
    }

    public static Path messagePath(String uid, String messageId) {
        return messagesPath(uid).child(messageId);
    }

    protected static String currentUserId() {
        return Fl.y.currentUserId();
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

    public static Path groupChatPath(String chatId) {
        return chatsPath().child(chatId);
    }

    public static Path groupChatMetaPath(String chatId) {
        return chatsPath().child(chatId).child(Meta);
    }

    public static Path groupChatMessagesPath(String chatId) {
        return groupChatPath(chatId).child(Messages);
    }

    public static Path groupChatUsersPath(String chatId) {
        return groupChatPath(chatId).child(Users);
    }

}
