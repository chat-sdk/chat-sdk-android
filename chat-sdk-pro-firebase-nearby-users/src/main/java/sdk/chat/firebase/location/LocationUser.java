package sdk.chat.firebase.location;

import android.location.Location;

import com.firebase.geofire.GeoLocation;

import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.utils.CurrentLocale;

/**
 * Created by ben on 4/3/18.
 */

public class LocationUser implements UserListItem {

    public User user;
    public GeoLocation location;

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

        double distance = distanceToMe();

        double minimumResolution = FirebaseNearbyUsersModule.config().minimumDisplayResolution;

        if (distance < minimumResolution) {
            distance = minimumResolution;
        }

        String formattedDistance;
        if (distance < 1000) {
            formattedDistance = String.format(CurrentLocale.get(), "%.0f m",distance);
        } else {
            distance = distance / 1000.0f;
            formattedDistance = String.format(CurrentLocale.get(), "%.0f km", distance);
        }
        if (minimumResolution != 0) {
            formattedDistance = "< "+formattedDistance;
        }
        return formattedDistance;
    }

    @Override
    public String getAvailability() {
        return user.getAvailability();
    }

    @Override
    public String getAvatarURL() {
        return user.getAvatarURL();
    }

    @Override
    public Boolean getIsOnline() {
        return user.getIsOnline();
    }

    public double distanceToMe() {
        Location currentLocation = FirebaseNearbyUsersModule.shared().getLocationHandler().getLocation();
        if (currentLocation != null && location != null) {
            GeoLocation toLocation = new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
            return Math.round(GeoFireManager.distanceBetween(location, toLocation));
        } else {
            return FirebaseNearbyUsersModule.config().maxDistance;
        }
    }

    @Override
    public String getEntityID() {
        return user.getEntityID();
    }
}
