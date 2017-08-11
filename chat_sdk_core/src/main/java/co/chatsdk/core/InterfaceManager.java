package co.chatsdk.core;

import co.chatsdk.core.interfaces.InterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class InterfaceManager {

    private final static InterfaceManager instance = new InterfaceManager();
    public InterfaceAdapter a;

    protected InterfaceManager () {

    }
    public static InterfaceManager shared () {
        return instance;
    }


}
