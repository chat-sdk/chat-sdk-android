package co.chatsdk.core.base;

import android.content.SharedPreferences;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Completable;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    protected boolean authenticatedThisSession = false;

    protected String currentUserID = null;

    protected boolean isAuthenticating = false;

    public void setIsAuthenticating(boolean isAuthenticating) {
        this.isAuthenticating = isAuthenticating;
    }

    public Boolean isAuthenticating () {
        return isAuthenticating;
    }

    protected void setAuthStateToIdle() {
        isAuthenticating = false;
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



}
