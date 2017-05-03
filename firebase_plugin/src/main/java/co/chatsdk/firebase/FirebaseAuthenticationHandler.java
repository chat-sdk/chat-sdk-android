package co.chatsdk.firebase;

import java.util.Map;

import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.types.AccountType;
import io.reactivex.Observable;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseAuthenticationHandler extends AbstractAuthenticationHandler {

    public Observable<Void> authenticateWithCachedToken() {
        return null;
    }

    public Observable<Void> authenticateWithMap (final Map<String, Object> details) {
        return null;
    }

    public Boolean userAuthenticated() {
        return false;
    }

    public Observable<Void> logout() {
        return null;
    }

    public Boolean accountTypeEnabled(AccountType type) {
        return null;
    }

    public Map<String, Object> loginInfo() {
        return null;
    }

    public void setLoginInfo(Map<String, Object> info) {

    }

    public String currentUserEntityID() {
        return null;
    }

}
