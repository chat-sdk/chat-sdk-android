package sdk.chat.micro;

public class Config {

    /**
     * Should the framework automatically send a delivery receipt when
     * a message is received
     */
    public boolean deliveryReceiptsEnabled = true;

    /**
     * Are group chat invites accepted automatically
     */
    public boolean autoAcceptGroupChatInvite = true;

    /**
     * If this is enabled, each time a message is received, it will be
     * deleted from our inbound message queue on Firestore. Even if this
     * is set to false, typing indicator messages and presence messages will
     * always be deleted as they don't have any use in the message archive
     */
    public boolean deleteMessagesOnReceipt = false;

    /**
     * How many historic messages should we retrieve?
     */
    public int messageHistoryLimit = 100;
}
