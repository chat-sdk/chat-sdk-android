package co.chatsdk.core.types;

import co.chatsdk.core.dao.Message;

/**
 * Created by benjaminsmiley-andrews on 08/05/2017.
 */

public class MessageUploadResult {

    public String imageURL;
    public String thumbnailURL;
    public Progress progress = new Progress();
    public Message message;

    public MessageUploadResult(String imageURL, String thumbnailURL) {
        this.imageURL = imageURL;
        this.thumbnailURL = thumbnailURL;
    }

    public boolean isComplete () {
        return imageURL != null && !imageURL.isEmpty() && thumbnailURL != null && !thumbnailURL.isEmpty();
    }

}
