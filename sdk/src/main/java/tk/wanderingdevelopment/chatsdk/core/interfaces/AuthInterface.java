package tk.wanderingdevelopment.chatsdk.core.interfaces;

import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.object.BError;

import org.jdeferred.Promise;

import java.util.Map;

/**
 * Created by KyleKrueger on 10.04.2017.
 */

public interface AuthInterface {




    Promise<Object, BError, Void> authenticateWithMap(Map<String, Object> details);

    void logout();

        /**
         * @return the save auth id saved in the preference manager.
         * The preference manager is initialized when the BNetworkManager.Init(context) is called.
         */
    String getCurrentUserAuthenticationId();

    // TODO: break into isAuthenticated and authenticateWithCachedToken
    Promise<BUser, BError, Void> checkUserAuthenticated();

    /** Send a password change request to the server.*/
    Promise<Void, BError, Void> changePassword(String email, String oldPassword, String newPassword);

    /** Send a reset email request to the server.*/
    Promise<Void, BError, Void> sendPasswordResetMail(String email);

    /**
     * Indicator that the current user in the adapter is authenticated.
     */
     boolean isAuthenticated();

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    void setLoginInfo(Map<String, Object> values);

     Map<String, ?> getLoginInfo();




}
