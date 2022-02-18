package sdk.chat.sinch;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class JWT {

    // ********************************************** IMPORTANT **********************************************
    //
    // The  JWT class serves as an example of how to produce and sign the registration token that you need to
    // initiate SinchClient, or UserController. Read more in the documentation online.
    //
    // DO NOT use this class in your application, instead implement the same functionality on your backend.
    // It might be tempting to re-use this class and store the APPLICATION_SECRET in your app, but that would
    // greatly compromise security.

    public static  String create(String appKey, String appSecret, String userId) {
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();
        final long issuedAt = System.currentTimeMillis() / 1000;
        String kid = "hkdfv1-" + formatDate(issuedAt);
        try {
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            header.put("kid", kid);
            payload.put("iss","//rtc.sinch.com/applications/" + appKey);
            payload.put("sub","//rtc.sinch.com/applications/" + appKey + "/users/" + userId);
            payload.put("iat",issuedAt);
            payload.put("exp",issuedAt + 600);
            payload.put("nonce", UUID.randomUUID());
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        String headerStr = header.toString().trim().replace("\\/","/");
        String payloadStr = payload.toString().trim().replace("\\/","/");
        String headerBase64 = Base64.encodeToString(headerStr.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
        String payloadBase64 = Base64.encodeToString(payloadStr.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);

        String jwtToSign = headerBase64 + "." + payloadBase64;

        String jwtSignature;
        try {
            byte[] origKey = Base64.decode(appSecret, Base64.DEFAULT);
            byte[] signingKey = deriveSigningKey(origKey, issuedAt);
            final byte[] macData = Hmac.hmacSha256(signingKey, jwtToSign);
            String signature = Base64.encodeToString(macData, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
            jwtSignature = jwtToSign + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return jwtSignature;
    }

    private static String formatDate(long time) {
        String format = "yyyyMMdd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(time * 1000));
    }

    private static byte[] deriveSigningKey(byte[] key, long issuedAt) {
        return Hmac.hmacSha256(key, formatDate(issuedAt));
    }
}


