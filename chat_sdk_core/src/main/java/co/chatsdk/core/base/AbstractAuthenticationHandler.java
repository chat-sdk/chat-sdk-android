package co.chatsdk.core.base;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.enums.AuthStatus;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.defines.Debug;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    private static final boolean DEBUG = Debug.AbstractAuthenticationHandler;
    public static String provider = "";
    public static String token = "";

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

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    public void setLoginInfo(Map<String, Object> values) {

        SharedPreferences.Editor keyValuesEditor = AppContext.getPreferences().edit();

        for (String s : values.keySet()) {
            if (values.get(s) instanceof Integer)
                keyValuesEditor.putInt(s, (Integer) values.get(s));
            else if (values.get(s) instanceof String)
                keyValuesEditor.putString(s, (String) values.get(s));
            else if (values.get(s) instanceof Boolean)
                keyValuesEditor.putBoolean(s, (Boolean) values.get(s));
            else if (DEBUG) Timber.e("Cant add this -->  %s to the prefs.", values.get(s));
        }

        keyValuesEditor.apply();
    }

    public void addLoginInfoData(String key, Object value){
        SharedPreferences.Editor keyValuesEditor = AppContext.getPreferences().edit();
        if (value instanceof Integer)
            keyValuesEditor.putInt(key, (Integer) value);
        else if (value instanceof String)
            keyValuesEditor.putString(key, (String) value);
        else if (DEBUG) Timber.e("Cant add this -->  %s to the prefs.", value);

        keyValuesEditor.apply();
    }

    /**
     * @return the save auth id saved in the preference manager.
     * The preference manager is initialized when the BNetworkManager.Init(context) is called.
     */
    public String getCurrentUserAuthenticationId() {
        return AppContext.getPreferences().getString(Defines.Prefs.AuthenticationID, "");
    }


    public Map<String, ?> getLoginInfo() {
        return AppContext.getPreferences().getAll();
    }


    public static Map<String, Object> getMap(String[] keys,  Object...values){
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0 ; i < keys.length; i++){

            // More values then keys entered.
            if (i == values.length)
                break;

            map.put(keys[i], values[i]);
        }

        return map;
    }


}
