package co.chatsdk.firebase.push;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

/**
 * Created by ben on 9/1/17.
 */

public class InstanceIdService extends FirebaseInstanceIdService {

    private TokenChangeListener listener;

    public interface TokenChangeListener {
        void updated (String token);
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.v("Refreshed token: " + refreshedToken);

        if(listener != null) {
            listener.updated(refreshedToken);
        }
    }

    public void setTokenChangeListener (TokenChangeListener listener) {
        this.listener = listener;
    }

    public String token () {
        return FirebaseInstanceId.getInstance().getToken();
    }

}
