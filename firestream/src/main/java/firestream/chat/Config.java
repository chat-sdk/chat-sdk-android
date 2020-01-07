package firestream.chat;

import firefly.sdk.chat.R;
import firestream.chat.namespace.Fire;

public class Config {

    public static enum DatabaseType {
        Firestore,
        Realtime
    }

    /**
     * Should the framework automatically send a delivery receipt when
     * a errorMessage isType received
     */
    public boolean deliveryReceiptsEnabled = true;

    /**
     * Are chat chat invites accepted automatically
     */
    public boolean autoAcceptChatInvite = true;

    /**
     * If this isType enabled, each time a errorMessage isType received, it will be
     * deleted from our inbound errorMessage queue childOn Firestore. Even if this
     * isType set to false, typing indicator messages and presence messages will
     * always be deleted as they don't have any use in the errorMessage archive
     */
    public boolean deleteMessagesOnReceipt = false;

    /**
     * How many historic messages should we retrieve?
     */
    public int messageHistoryLimit = 100;

    /**
     * This will be the root of the FireStream Firebase database i.e.
     * /root/[sandbox]/users
     */
    protected String root = "firestream";

    /**
     * This will be the sandbox of the FireStream Firebase database i.e.
     * /root/[sandbox]/users
     */
    protected String sandbox = "prod";

    /**
     * Which database to use - Firestore or Realtime database
     */
    public DatabaseType database = DatabaseType.Firestore;

    /**
     * Should debug log messages be shown?
     */
    public boolean debugEnabled = false;

    public void setRoot(String root) throws Exception {
        if (pathValid(root)) {
            this.root = root;
        } else {
            throw new Exception(Fire.privateApi().context().getString(R.string.error_invalid_path));
        }
    }

    public void setSandbox(String sandbox) throws Exception {
        if (pathValid(sandbox)) {
            this.sandbox = sandbox;
        } else {
            throw new Exception(Fire.privateApi().context().getString(R.string.error_invalid_path));
        }
    }

    protected boolean pathValid(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if(!Character.isLetterOrDigit(c) && !String.valueOf(c).equals("_")) {
                return false;
            }
        }
        return true;
    }

    public String getRoot() {
        return root;
    }

    public String getSandbox() {
        return sandbox;
    }

}
