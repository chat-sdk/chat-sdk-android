package sdk.chat.encryption;

import com.virgilsecurity.common.util.Base64;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;

public class PrivateKey {

    public VirgilPrivateKey key;

    public PrivateKey(byte [] privateKeyBytes) throws Exception {
        VirgilCrypto crypto = new VirgilCrypto();
        key = crypto.importPrivateKey(privateKeyBytes).getPrivateKey();
    }

    public PrivateKey(String privateKeyString) throws Exception  {
        this(Base64.decode(privateKeyString));
    }

    public PrivateKey(VirgilPrivateKey key) {
        this.key = key;
    }

    public byte [] getBytes() throws Exception {
        VirgilCrypto crypto = new VirgilCrypto();
        return crypto.exportPrivateKey(key);
    }

    public String getBase64EncodedString() throws Exception {
        return Base64.encode(getBytes());
    }

    public String getId() {
        return Base64.encode(key.getIdentifier());
    }
}
