package co.chatsdk.core;

import co.chatsdk.core.interfaces.StorageAdapter;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class StorageManager {

    private static StorageManager instance;
    public StorageAdapter a;

    public static StorageManager shared(){
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

}
