package sdk.chat.core.manager;

import android.graphics.drawable.Drawable;

public interface MessagePayload {

    String getText();
    String imageURL();

    /**
     * This is what is shown on the threads screen
     * @return
     */
    String lastMessageText();

    MessagePayload replyPayload();
    Drawable getPlaceholder();

}
