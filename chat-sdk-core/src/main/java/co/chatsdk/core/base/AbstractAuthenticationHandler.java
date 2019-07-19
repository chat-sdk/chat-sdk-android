package co.chatsdk.core.base;

import android.content.SharedPreferences;

import java.util.Map;

import co.chatsdk.core.enums.AuthStatus;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AuthKeys;
import io.reactivex.Completable;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    public static String provider = "";
    protected boolean authenticatedThisSession = false;

    private AuthStatus authStatus = AuthStatus.IDLE;

    public AuthStatus getAuthStatus () {
        return authStatus;
    }

    public void setAuthStatus (AuthStatus status) {
        authStatus = status;
    }

    public boolean isAuthenticating () {
        return authStatus != AuthStatus.IDLE;
    }

    protected void setAuthStateToIdle() {
        authStatus = AuthStatus.IDLE;
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
    public void setLoginInfo(Map<String, Object> values) {

        SharedPreferences.Editor keyValuesEditor = ChatSDK.shared().getPreferences().edit();

        for (String s : values.keySet()) {
            if (values.get(s) instanceof Integer)
                keyValuesEditor.putInt(s, (Integer) values.get(s));
            else if (values.get(s) instanceof String)
                keyValuesEditor.putString(s, (String) values.get(s));
            else if (values.get(s) instanceof Boolean)
                keyValuesEditor.putBoolean(s, (Boolean) values.get(s));
        }

        keyValuesEditor.apply();
    }

    public void addLoginInfoData (String key, Object value) {
        SharedPreferences.Editor keyValuesEditor = ChatSDK.shared().getPreferences().edit();
        if (value instanceof Integer) {
            keyValuesEditor.putInt(key, (Integer) value);
        }
        else if (value instanceof String) {
            keyValuesEditor.putString(key, (String) value);
        }

        keyValuesEditor.apply();
    }

    public void removeLoginInfo (String key) {
        SharedPreferences.Editor keyValuesEditor = ChatSDK.shared().getPreferences().edit();
        keyValuesEditor.remove(key);
        keyValuesEditor.apply();
    }

    /**
     * @return the save auth id saved in the preference manager.
     * The preference manager is initialized when the NetworkManager.Init(context) is called.
     */
    public String getCurrentUserEntityID() {
        return (String) getLoginInfo().get(AuthKeys.CurrentUserID);
    }

    public Map<String, ?> getLoginInfo() {
        return ChatSDK.shared().getPreferences().getAll();
    }



}
