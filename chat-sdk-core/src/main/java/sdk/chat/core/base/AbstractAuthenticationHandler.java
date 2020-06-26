package sdk.chat.core.base;

import android.content.SharedPreferences;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.session.ChatSDK;
import io.reactivex.Completable;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    protected boolean authenticatedThisSession = false;

    protected String currentUserID = null;

    protected Completable authenticating;

    protected Completable loggingOut;

    public Boolean isAuthenticating () {
        return authenticating != null;
    }

    protected void setAuthStateToIdle() {
        authenticating = null;
    }

    @Deprecated
    public Boolean userAuthenticated() {
        return isAuthenticated();
    }

    @Deprecated
    public Boolean userAuthenticatedThisSession () {
        return isAuthenticatedThisSession();
    }

    public Boolean isAuthenticatedThisSession () {
        return isAuthenticated() && authenticatedThisSession;
    }

    @Deprecated
    public Completable authenticateWithCachedToken () {
        return authenticate();
    }

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    public void saveCurrentUserEntityID(String currentUserID) {

        this.currentUserID = currentUserID;

        SharedPreferences.Editor keyValuesEditor = ChatSDK.shared().getPreferences().edit();
        keyValuesEditor.putString(Keys.CurrentUserID, currentUserID);
        keyValuesEditor.apply();
    }

    public void clearSavedCurrentUserEntityID() {

        currentUserID = null;

        SharedPreferences.Editor keyValuesEditor = ChatSDK.shared().getPreferences().edit();
        keyValuesEditor.remove(Keys.CurrentUserID);
        keyValuesEditor.apply();
    }

    /**
     * @return the save auth id saved in the preference manager.
     * The preference manager is initialized when the NetworkManager.Init(context) is called.
     */
    public String getCurrentUserEntityID() {
        if (currentUserID == null || !isAuthenticated()) {
            currentUserID = getSavedCurrentUserEntityID();
        }
        return currentUserID;
    }


    public String getSavedCurrentUserEntityID() {
        return ChatSDK.shared().getPreferences().getString(Keys.CurrentUserID, null);
    }

    public void cancel() {
        if (isAuthenticating()) {
            authenticating = null;
        }
    }

    public void stop() {
        authenticatedThisSession = false;
        currentUserID = null;
        authenticating = null;
        loggingOut = null;
    }
}
