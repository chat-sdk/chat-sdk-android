package tk.wanderingdevelopment.chatsdk.core.abstracthandlers;

import android.content.Context;
import android.content.SharedPreferences;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;

import org.jdeferred.Promise;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.interfaces.AuthInterface;

import static com.braunster.chatsdk.network.BDefines.Prefs.AuthenticationID;

/**
 * Created by KyleKrueger on 10.04.2017.
 */

public abstract class AuthManager implements AuthInterface {

    private boolean authenticated = false;
    private static final boolean DEBUG = Debug.AbstractNetworkAdapter;
    public static String provider = "";
    public static String token = "";

    protected Context context;

    protected AuthManager(Context ctx){
        this.context = ctx.getApplicationContext();
    }

    public abstract Promise<Object, BError, Void> authenticateWithMap(Map<String, Object> details);

    public abstract void logout();

        // TODO: break into isAuthenticated and authenticateWithCachedToken
    public abstract Promise<BUser, BError, Void> checkUserAuthenticated();

    /** Send a password change request to the server.*/
    public abstract Promise<Void, BError, Void> changePassword(String email, String oldPassword, String newPassword);

    /** Send a reset email request to the server.*/
    public abstract Promise<Void, BError, Void> sendPasswordResetMail(String email);

    /**
     * Indicator that the current user in the adapter is authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Set the current status of the adapter to not authenticated.
     * The status can be retrieved by calling "isAuthenticated".
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    public void setLoginInfo(Map<String, Object> values) {

        SharedPreferences.Editor keyValuesEditor = BNetworkManager.preferences.edit();

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
        SharedPreferences.Editor keyValuesEditor = BNetworkManager.preferences.edit();
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
        return BNetworkManager.preferences.getString(AuthenticationID, "");
    }


    public Map<String, ?> getLoginInfo() {
        return BNetworkManager.preferences.getAll();
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
