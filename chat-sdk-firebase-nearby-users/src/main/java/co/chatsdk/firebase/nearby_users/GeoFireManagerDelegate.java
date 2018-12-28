package co.chatsdk.firebase.nearby_users;

import com.firebase.geofire.GeoLocation;

import co.chatsdk.core.dao.User;

/**
 * Created by ben on 4/3/18.
 */

public interface GeoFireManagerDelegate {

    void userAdded (User user, GeoLocation location);
    void userRemoved (User user);
    void userMoved (User user, GeoLocation location);

}
