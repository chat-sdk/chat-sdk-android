package sdk.chat.core.base;

import io.reactivex.Completable;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
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
    private User cachedUser = null;

    public Boolean isAuthenticating () {
        return authenticating != null;
    }

    protected void setAuthStateToIdle() {
        authenticating = null;
        loggingOut = null;
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
        cachedUser = null;
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

    @Override
    public User currentUser() {
        String entityID = ChatSDK.auth().getCurrentUserEntityID();

        if (entityID == null) {
            cachedUser = null;
        }

        if(cachedUser == null || !cachedUser.equalsEntityID(entityID)) {
            if (entityID != null && !entityID.isEmpty()) {
                cachedUser = DaoCore.fetchEntityWithEntityID(User.class, entityID);
            }
            else {
                cachedUser = null;
            }
        }
        return cachedUser;
    }

    public void cancel() {
        if (isAuthenticating()) {
            authenticating = null;
        }
    }

    public void stop() {
        cachedUser = null;
        currentUserID = null;
        authenticating = null;
        isAuthenticatedThisSession = false;
        loggingOut = null;
    }
}
