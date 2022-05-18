package sdk.chat.core.manager;

import java.util.List;

import io.reactivex.Completable;

public interface MessagePayload {

    String getText();
    String imageURL();

    /**
     * This is what is shown on the threads screen
     * @return
     */
    String previewText();

    @Deprecated
    // TODO: DO this with cached files
    List<String> remoteURLs();
    Completable downloadMessageContent();
    MessagePayload replyPayload();

}
