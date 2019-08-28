package co.chatsdk.core.handlers;

/**
 * Created by Pepe Becker on 27/08/2019.
 */

public interface CallListener {

    void onCallEnded(Call call);
    void onCallEstablished(Call call);
    void onCallProgressing(Call call);
    void onVideoPaused(Call call);
    void onVideoResumed(Call call);

}
