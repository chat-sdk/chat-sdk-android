package sdk.chat.encryption;

import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.utils.Base64;

public class KeyPair {

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public KeyPair(byte [] privateKeyBytes, byte [] publicKeyBytes) throws Exception {
        privateKey = new PrivateKey(privateKeyBytes);
        publicKey = new PublicKey(publicKeyBytes);
    }

    public KeyPair(String privateKeyString, String publicKeyString) throws Exception {
        this(Base64.decode(privateKeyString), Base64.decode(publicKeyString));
    }

    public KeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public KeyPair(VirgilKeyPair pair) {
        this.privateKey = new PrivateKey(pair.getPrivateKey());
        this.publicKey = new PublicKey(pair.getPublicKey());
    }

}
