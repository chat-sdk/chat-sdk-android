package sdk.chat.app.xmpp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

import sdk.chat.core.utils.KeyStorage;

public class SecureKeyStore extends KeyStorage {

    String masterKeyAlias;
    SharedPreferences prefs;

    public SecureKeyStore(Context context) throws GeneralSecurityException, IOException {
        super(context);
        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        prefs = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public SharedPreferences pref() {
        return prefs;
    }

}
