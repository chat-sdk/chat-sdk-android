package sdk.chat.encryption;

import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.utils.Base64;

public class PublicKey {

    public VirgilPublicKey key;

    public PublicKey(byte [] publicKeyBytes) throws Exception {
        VirgilCrypto crypto = new VirgilCrypto();
        key = crypto.importPublicKey(publicKeyBytes);
    }

    public PublicKey(String publicKeyString) throws Exception  {
        this(Base64.decode(publicKeyString));
    }

    public PublicKey(VirgilPublicKey key) {
        this.key = key;
    }

    public byte [] getBytes() throws Exception {
        VirgilCrypto crypto = new VirgilCrypto();
        return crypto.exportPublicKey(key);
    }

    public String getBase64EncodedString() throws Exception {
        return Base64.encode(getBytes());
    }

}
