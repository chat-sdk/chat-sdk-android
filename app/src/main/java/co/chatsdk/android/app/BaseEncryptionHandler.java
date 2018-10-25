package co.chatsdk.android.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.utils.Base64;

import org.greenrobot.greendao.database.Database;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.EncryptionHandler;
import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.main.MainActivity;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static android.media.CamcorderProfile.get;
import static co.chatsdk.core.session.ChatSDK.currentUser;

public class BaseEncryptionHandler implements EncryptionHandler {

    public static String PrivateKey = "private-key";
    public static String PublicKey = "public-key";
    public final VirgilCrypto virgilCrypto = new VirgilCrypto();

    // Here are the hooks, one for a successful authentication and one for a recieved message

    public BaseEncryptionHandler() {

        Hook hook = new Hook(data -> {
            Object message = data.get(BaseHookHandler.MessageReceived_Message);
            if (message != null && message instanceof Message) {
                decrypt((Message) message);
            }
        });
        ChatSDK.hook().addHook(hook, BaseHookHandler.MessageReceived);

        Hook auth = new Hook(data -> {

            // This code will run here, when the user is logged in
            // Must upload the user's public key to the user'r profile
            User currentUser = currentUser();

            try {
                VirgilKeyPair keyPair = getKeys(currentUser.getEntityID());
                byte [] identifierBytes = keyPair.getPublicKey().getIdentifier();
                String identifierString = Base64.encode(identifierBytes);
                String identifierEncoded = URLEncoder.encode(identifierString, "UTF-8");
                VirgilPublicKey publicKey = keyPair.getPublicKey();
                byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
                String encodedPublicKey = Base64.encode(publicKeyBytes);
                currentUser().setMetaString(PublicKey + identifierEncoded, encodedPublicKey);
                ChatSDK.core().pushUser().subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Timber.d("Success");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Timber.d("Error");
                    }
                });
            }
            catch (Exception e) {
            }
        });
        ChatSDK.hook().addHook(auth, BaseHookHandler.UserAuthFinished);

    }

    //Checking if the key was saved before. If so, the key is retrieved. If not, the key is generated and stored.


    public VirgilKeyPair getKeys(String entityID) throws Exception {

        // We can have multiple private keys because we can have multiple users on one device
        // Append the users entity iD to the private key key

        String privateKeyString = ChatSDK.shared().getPreferences().getString(PrivateKey + entityID, null);
        String publicKeyString = ChatSDK.shared().getPreferences().getString(PublicKey, null);

        VirgilKeyPair keyPair;

        if (privateKeyString == null || privateKeyString.isEmpty() || publicKeyString == null || publicKeyString.isEmpty()) {

            keyPair = virgilCrypto.generateKeys();
            VirgilPublicKey publicKey = keyPair.getPublicKey();
            VirgilPrivateKey privateKey = keyPair.getPrivateKey();

            byte [] privateKeyBytes = virgilCrypto.exportPrivateKey(privateKey);
            privateKeyString = Base64.encode(privateKeyBytes);
            byte [] publicKeyBytes = virgilCrypto.exportPublicKey(publicKey);
            publicKeyString = Base64.encode(publicKeyBytes);

            ChatSDK.shared().getPreferences().edit().putString(PrivateKey, privateKeyString).apply();
            ChatSDK.shared().getPreferences().edit().putString(PublicKey, publicKeyString).apply();

        }
        else {

            byte [] privateKeyBytes = Base64.decode(privateKeyString);
            byte [] publicKeyBytes = Base64.decode(publicKeyString);

            VirgilPrivateKey privateKey = virgilCrypto.importPrivateKey(privateKeyBytes);
            VirgilPublicKey publicKey = virgilCrypto.importPublicKey(publicKeyBytes);
            keyPair = new VirgilKeyPair(publicKey, privateKey);
        }

        return keyPair;
    }

    // Here is where we actually encrypt and decrypt the messages;

    @Override
    public void encrypt(Message message) {

        List<User> users = message.getThread().getUsers();
        List<VirgilPublicKey> publicKeyList = new ArrayList<>();

        try {
            for(User u : users) {
                // Loop over meta values and see if any key matches your pattern like "public-key"
                // Key could look like public-key-[identifier]
                Map<String, String> meta = u.metaMap();

                for (String key : meta.keySet()) {
                    if (key.contains(PublicKey)) {
                        String publicKey = meta.get(key);
                        if (publicKey != null && !publicKey.isEmpty()) {
                            byte [] keyBytes = Base64.decode(publicKey);
                            VirgilPublicKey virgilPublicKey = virgilCrypto.importPublicKey(keyBytes);
                            publicKeyList.add(virgilPublicKey);
                        }
                    }
                }
            }

            if (publicKeyList.size() > 0) {
                // What do we do if there are no public keys? Create an error message on the screen
                byte [] messageBytes = Base64.decode(message.getTextString());
                byte [] encryptedMessage = virgilCrypto.encrypt(messageBytes, publicKeyList);
                String encryptedMessageString = Base64.encode(encryptedMessage);
                message.setTextString(encryptedMessageString);
            }
        }
        catch (Exception e) {
        }
    }

    @Override
    public void decrypt(Message message) {
        try {
            VirgilKeyPair keyPair = getKeys(ChatSDK.currentUser().getEntityID());
            VirgilPrivateKey privateKey = keyPair.getPrivateKey();

            // Base 64 decode
            byte [] decryptedMessageBytes = virgilCrypto.decrypt(message.getTextString().getBytes(), privateKey);
            String decryptedMessageString = new String(decryptedMessageBytes);
            message.setTextString(decryptedMessageString);
        }
        catch (Exception e) {
        }
    }
}

// DO I want to instantiate one Virgil Crypto for the entire class or do I want to instantiate it each time I use it in each method?

