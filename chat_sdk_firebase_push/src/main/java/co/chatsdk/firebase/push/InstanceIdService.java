package co.chatsdk.firebase.push;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

/**
 * Created by ben on 9/1/17.
 */

public class InstanceIdService extends FirebaseInstanceIdService {

    public interface TokenChangeListener {
        void updated (String token);
    }

    @Override
    public void onTokenRefresh() {
        Timber.v("Refreshed token: " + token());
        TokenChangeConnector.shared().updated(token());
    }

    public String token () {
        return FirebaseInstanceId.getInstance().getToken();
    }

}
