package co.chatsdk.firebase.nearby_users;

import com.firebase.geofire.GeoLocation;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;

/**
 * Created by ben on 4/3/18.
 */

public class LocationUser implements UserListItem {

    private User user;
    private GeoLocation location;

    public GeoLocation referenceLocation;

    public LocationUser (User user, GeoLocation location) {
        this.user = user;
        this.location = location;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getStatus() {
        return this.user.getStatus();
    }

    @Override
    public String getAvailability() {
        return user.getAvailability();
    }

    @Override
    public String getAvatarURL() {
        return user.getAvatarURL();
    }

    public User getUser() {
        return user;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public double distanceToReference () {
        if (location != null && referenceLocation != null) {
            return Math.round(GeoFireManager.distanceBetween(location, referenceLocation));
        }
        else {
            return 99999999;
        }
    }

    public String getDistanceText() {
        double distance = distanceToReference();
        if (distance < 1000) {
            return String.format("%.0f m", distance);
        }
        else {
            distance = distance / 1000.0f;
            return String.format("%.0f km", distance);
        }
    }
}
