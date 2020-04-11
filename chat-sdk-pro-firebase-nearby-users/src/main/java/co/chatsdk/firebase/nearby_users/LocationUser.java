package co.chatsdk.firebase.nearby_users;

import com.firebase.geofire.GeoLocation;

import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;

/**
 * Created by ben on 4/3/18.
 */

public class LocationUser implements UserListItem {

    public User user;
    public GeoLocation location;
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
        double distance = distanceToReference();

        Object minimumResolutionObject = ChatSDK.config().getCustomProperty(CustomProperties.MinimumResolution);
        if (minimumResolutionObject instanceof Double) {
            Double minimumDistance = (Double) minimumResolutionObject;
            if (distance < minimumDistance) {
                distance = minimumDistance;
            }
        }

        String formattedDistance;
        if (distance < 1000) {
            formattedDistance = String.format("%.0f",distance) + " m";
        } else {
            distance = distance / 1000.0f;
            formattedDistance = String.format("%.0f km", distance);
        }
        if (minimumResolutionObject != null) {
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

    public double distanceToReference () {
        if (location != null && referenceLocation != null) {
            return Math.round(GeoFireManager.distanceBetween(location, referenceLocation));
        }
        else {
            return 99999999;
        }
    }

    @Override
    public String getEntityID() {
        return user.getEntityID();
    }
}
