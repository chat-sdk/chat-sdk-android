package sdk.chat.core.base;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    protected String currentUserID = null;
    protected boolean isAuthenticatedThisSession = false;

    protected Completable authenticating;

    protected Completable loggingOut;

    public Boolean isAuthenticating () {
        return authenticating != null;
    }

    protected void setAuthStateToIdle() {
        authenticating = null;
    }

    public Boolean isAuthenticatedThisSession() {
        return isAuthenticated() && isAuthenticatedThisSession;
    }

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    public void setCurrentUserEntityID(String currentUserID) {

        this.currentUserID = currentUserID;
        isAuthenticatedThisSession = true;
        ChatSDK.shared().getKeyStorage().put(Keys.CurrentUserID, currentUserID);

    }

    public void clearCurrentUserEntityID() {

        currentUserID = null;
        isAuthenticatedThisSession = false;
        ChatSDK.shared().getKeyStorage().remove(Keys.CurrentUserID);

    }

    /**
     * @return the save auth id saved in the preference manager.
     * The preference manager is initialized when the NetworkManager.Init(context) is called.
     */
    public String getCurrentUserEntityID() {
        if (currentUserID == null || !isAuthenticated()) {
            currentUserID = ChatSDK.shared().getKeyStorage().get(Keys.CurrentUserID);
        }
        return currentUserID;
    }

    public void cancel() {
        if (isAuthenticating()) {
            authenticating = null;
        }
    }

    public void stop() {
        currentUserID = null;
        authenticating = null;
        isAuthenticatedThisSession = false;
        loggingOut = null;
    }
}
