package co.chatsdk.firebase.push;

import java.util.ArrayList;

/**
 * Created by ben on 9/13/17.
 */

public class TokenChangeConnector {

    private static final TokenChangeConnector instance = new TokenChangeConnector();

    private ArrayList<InstanceIdService.TokenChangeListener> listeners = new ArrayList<>();

    public static TokenChangeConnector shared () {
        return instance;
    }

    public void addListener (InstanceIdService.TokenChangeListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener (InstanceIdService.TokenChangeListener listener) {
        if(listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void updated (String token) {
        for(InstanceIdService.TokenChangeListener l : listeners) {
            l.updated(token);
        }
    }

}
