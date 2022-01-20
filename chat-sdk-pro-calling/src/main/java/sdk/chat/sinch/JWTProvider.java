package sdk.chat.sinch;

import io.reactivex.Single;

public interface JWTProvider {
    Single<String> getJWT(String userId);
}
