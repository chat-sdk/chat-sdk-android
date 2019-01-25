package co.chatsdk.core.session;

import co.chatsdk.core.base.BaseLocationAdapter;

/**
 * Created by Pepe on 01/25/19.
 */

public class LocationManager {

    private final static LocationManager instance = new LocationManager();
    public BaseLocationAdapter a;

    protected LocationManager() {}

    public static LocationManager shared() {
        return instance;
    }

}
