package sdk.chat.sinch;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class Hmac {

    private Hmac() {}

    public static byte[] hmacSha256(byte[] key, String message) {
        if (null == key || key.length == 0)
            throw new IllegalArgumentException("Invaid input key to HMAC-256");

        if (null == message)
            throw new IllegalArgumentException("Input message to HMAC-256 must not be null");

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(message.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
