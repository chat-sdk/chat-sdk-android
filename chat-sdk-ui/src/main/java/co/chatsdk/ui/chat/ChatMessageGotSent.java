package co.chatsdk.ui.chat;

public class ChatMessageGotSent {

    public boolean success;
    public boolean isImage;

    public ChatMessageGotSent(boolean success, boolean isImage) {
        success = this.success;
        isImage = this.isImage;
    }

}
