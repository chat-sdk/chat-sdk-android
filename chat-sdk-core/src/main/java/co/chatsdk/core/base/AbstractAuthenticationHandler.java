package co.chatsdk.core.base;

import android.content.SharedPreferences;

import java.util.Map;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.enums.AuthStatus;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.AuthKeys;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    public static String provider = "";

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

    public Completable authenticateWithMap (final Map<String, Object> details) {
        return Single.create(new SingleOnSubscribe<AccountDetails>() {
            @Override
            public void subscribe(SingleEmitter<AccountDetails> e) throws Exception {

                AccountDetails accountDetails = new AccountDetails();

                int loginType = (Integer) details.get(AuthKeys.Type);

                String password = (String) details.get(AuthKeys.Password);
                String email = (String) details.get(AuthKeys.Email);
                String token = (String) details.get(AuthKeys.Token);

                switch (loginType) {
                    case AccountType.Password:
                        accountDetails = AccountDetails.username(email, password);
                        break;
                    case AccountType.Register:
                        accountDetails = AccountDetails.signUp(email, password);
                        break;
                    case AccountType.Facebook:
                        accountDetails = AccountDetails.facebook();
                        break;
                    case AccountType.Twitter:
                        accountDetails = AccountDetails.twitter();
                        break;
                    case AccountType.Google:
                        accountDetails = AccountDetails.google();
                        break;
                    case AccountType.Anonymous:
                        accountDetails = AccountDetails.anonymous();
                        break;
                    case AccountType.Custom:
                        accountDetails = AccountDetails.token(token);
                        break;
                }

                if(accountDetails != null) {
                    e.onSuccess(accountDetails);
                }
                else {
                    e.onError(new Throwable("No valid login method defined"));
                }
            }
        }).flatMapCompletable(new Function<AccountDetails, Completable>() {
            @Override
            public Completable apply(AccountDetails accountDetails) throws Exception {
                return authenticate(accountDetails);
            }
        }).subscribeOn(Schedulers.io());
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
