package wanderingdevelopment.tk.chatsdkcore.db;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import timber.log.Timber;
import wanderingdevelopment.tk.chatsdkcore.BuildConfig;
import wanderingdevelopment.tk.chatsdkcore.entities.AuthCredential;

/**
 * Created by kykrueger on 2017-01-10.
 */

public class DBAuthAdapter extends DBAdapter{

    public DBAuthAdapter(Context context) {
        super(context);
    }

    public void storeCredentials(String userAlias, String password){
        SharedPreferences sharedPreferences = DaoCore.getSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor prefEditor;
        prefEditor = sharedPreferences.edit();
        prefEditor.putString(DaoCore.PREFERENCE_KEY_DATABASE_ID, userAlias);
        prefEditor.apply();

        DaoCore.getDaoCore(getContext(), userAlias);

        AuthCredential authCredential;
        authCredential = new AuthCredential();
        authCredential.setUserAlias(userAlias);
        authCredential.setUserPassword(password);
        getDaoCore().createEntity(authCredential);
    }

    public AuthCredential fetchCredentials(){
        AuthCredential authCredential;
        String userAlias;

        SharedPreferences sharedPreferences = DaoCore.getSharedPreferences(getContext().getApplicationContext());

        userAlias = sharedPreferences.getString(DaoCore.PREFERENCE_KEY_DATABASE_ID, null);

        if ( userAlias == null) return null;

        authCredential = getDaoCore().fetchEntityWithProperty(AuthCredential.class,
                AuthCredentialDao.Properties.UserAlias,
                userAlias);

        return authCredential;
    }

    public void logout(){
        DaoCore.resetDaoCore(getContext());
        removeCredentials(); // must happen after the default daocore is restored
    }
    private void removeCredentials(){
        SharedPreferences sharedPreferences = DaoCore.getSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor prefEditor;
        prefEditor = sharedPreferences.edit();
        prefEditor.putString(DaoCore.PREFERENCE_KEY_DATABASE_ID,
                DaoCore.PREFERENCE_VALUE_DEFAULT_EMPTY);
        prefEditor.apply();

        getDaoCore().deleteEntity(fetchCredentials());
    }
}
