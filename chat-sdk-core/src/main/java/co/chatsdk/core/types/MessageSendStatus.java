package co.chatsdk.core.types;

/**
 * Created by ben on 9/29/17.
 */

public enum MessageSendStatus {
    None,
    Created,
    WillUpload,
    Uploading,
    DidUpload,
    WillSend,
    Sending,
    Sent,
    Delivered,
    Failed,
}
