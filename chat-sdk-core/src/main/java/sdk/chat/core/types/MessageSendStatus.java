package sdk.chat.core.types;

/**
 * Created by ben on 9/29/17.
 */

public enum MessageSendStatus {
    None,
    Created,
    Compressing,
    WillUpload,
    Uploading,
    DidUpload,
    WillSend,
    Sent,
    Failed,
}
