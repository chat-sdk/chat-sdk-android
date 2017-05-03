package co.chatsdk.core;

/**
 * Created by benjaminsmiley-andrews on 28/04/2017.
 */

public class NetworkManager {

    private static NetworkManager instance;
    public AbstractNetworkAdapter a;

    public static NetworkManager sharedManager(){
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

}
