package firestream.chat;

import org.pmw.tinylog.Logger;

import sdk.guru.common.BaseConfig;

public class FirestreamConfig<T> extends BaseConfig<T> {

    public FirestreamConfig(T onBuild) {
        super(onBuild);
    }

    public boolean deliveryReceiptsEnabled = true;
    public boolean autoMarkReceived = true;
    public boolean autoAcceptChatInvite = true;
    public boolean deleteMessagesOnReceipt = false;
    public boolean emitEventForLastMessage = false;
    protected String root = "firestream";
    protected String sandbox = "prod";

    // Debug and testing settings
    public boolean debugEnabled = false;
    public boolean deleteDeliveryReceiptsOnReceipt = true;

    /**
     * This will be the root of the FireStream Firebase database i.e.
     * /root/[sandbox]/users
     */
    public FirestreamConfig<T> setRoot(String root) {
        String path = validatePath(root);
        if (path != null) {
            this.root = path;
        }
        return this;
    }

    public FirestreamConfig<T> setSandbox(String sandbox) {
        String path = validatePath(sandbox);
        if (path != null) {
            this.sandbox = path;
        }
        return this;
    }

    /**
     * Should the framework automatically send a delivery receipt when
     * a message type received
     */
    public FirestreamConfig<T> setDeliveryReceiptsEnabled(boolean value) {
        this.deliveryReceiptsEnabled = value;
        return this;
    }

    /**
     * Should the framework send the received receipt automatically
     */
    public FirestreamConfig<T> setAutoMarkReceivedEnabled(boolean value) {
        this.autoMarkReceived = value;
        return this;
    }

    /**
     * Are chat chat invites accepted automatically
     */
    public FirestreamConfig<T> setAutoAcceptChatInviteEnabled(boolean value) {
        this.autoAcceptChatInvite = value;
        return this;
    }

    /**
     * If this type enabled, each time a message type received, it will be
     * deleted from our inbound message queue childOn Firestore. Even if this
     * type set to false, typing indicator messages and presence messages will
     * always be deleted as they don't have any use in the message archive
     * this flag only affects 1-to-1 messages.
     */
    public FirestreamConfig<T> setDeleteMessagesOnReceiptEnabled(boolean value) {
        this.deleteMessagesOnReceipt = value;
        return this;
    }

    /**
     * If this is set to true, an event will be emitted for the last message received
     * this may be a duplicate.
     */
    public FirestreamConfig<T> setEmitEventForLastMessage(boolean enabled) {
        emitEventForLastMessage = enabled;
        return this;
    }

    /**
     * Should debug log messages be shown?
     */
    public FirestreamConfig<T> setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
        return this;
    }

    /**
     * Should debug log messages be shown?
     */
    public FirestreamConfig<T> setDeleteDeliveryReceiptsOnReceipt(boolean enabled) {
        deleteDeliveryReceiptsOnReceipt = enabled;
        return this;
    }

    protected String validatePath(String path) {
        if (path != null) {
            String validPath = path.replaceAll("[^a-zA-Z0-9_]", "");
            if (!validPath.isEmpty()) {
                if (!validPath.equals(path)) {
                    Logger.warn("The root path cannot contain special characters, they were removed so your new root path is: " + validPath);
                }
                return validPath;
            } else {
                Logger.warn("The root path cannot contain special characters, when removed your root path was empty so the default was used instead");
            }
        } else {
            Logger.warn("The root path provided cannot be null, the default was used instead");
        }
        return null;
    }

    public String getRoot() {
        return root;
    }

    public String getSandbox() {
        return sandbox;
    }

}
