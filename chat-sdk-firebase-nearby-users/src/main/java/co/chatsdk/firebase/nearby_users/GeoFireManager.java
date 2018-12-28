package co.chatsdk.firebase.nearby_users;

import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.lang.ref.WeakReference;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by ben on 4/3/18.
 */

public class GeoFireManager {

    // Radius to search in km
    public static float GeoLocationRadius = 100;

    private WeakReference<GeoFireManagerDelegate> delegate;
    private GeoLocation location;
    GeoQueryEventListener listener;


    public GeoFireManager (GeoFireManagerDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);

        ChatSDK.hook().addHook(new Hook(data -> {
            location = null;
        }), BaseHookHandler.Logout);

        ChatSDK.hook().addHook(new Hook(data -> {
            location = null;
        }), BaseHookHandler.SetUserOffline);

    }

    public void findNearbyUsersWithRadius (double latitude, double longitude, float radius) {

        GeoFire geoFire = geoFireRef();
        GeoLocation center = new GeoLocation(latitude, longitude);
        GeoQuery query = geoFire.queryAtLocation(center, radius);

        User currentUser = ChatSDK.currentUser();

        if (listener != null) {
            try {
                query.removeGeoQueryEventListener(listener);
            }
            catch (IllegalArgumentException e) {
                ChatSDK.logError(e);
            }
        }

        listener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String entityID, GeoLocation location) {
                if (!currentUser.getEntityID().equals(entityID)) {
                    final UserWrapper wrapper = UserWrapper.initWithEntityId(entityID);
                    wrapper.once().observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                        delegate.get().userAdded(wrapper.getModel(), location);
                    });
                }
            }

            @Override
            public void onKeyExited(String entityID) {
                if (!currentUser.getEntityID().equals(entityID)) {
                    final UserWrapper wrapper = UserWrapper.initWithEntityId(entityID);
                    wrapper.once().observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                        delegate.get().userRemoved(wrapper.getModel());
                    });
                }
            }

            @Override
            public void onKeyMoved(String entityID, GeoLocation location) {
                if (!currentUser.getEntityID().equals(entityID)) {
                    final UserWrapper wrapper = UserWrapper.initWithEntityId(entityID);
                    wrapper.once().observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                        delegate.get().userMoved(wrapper.getModel(), location);
                    });
                }
            }

            @Override
            public void onGeoQueryReady() {}

            @Override
            public void onGeoQueryError(DatabaseError error) {}
        };

        query.addGeoQueryEventListener(listener);

    }

    public void setLocation (double latitude, double longitude) {

        if (location != null) {
            // Work out the distance between the new location and the old one

            double distance = distanceBetween(new GeoLocation(latitude, longitude), location);
            if (distance < 100) {
                return;
            }
        }

        location = new GeoLocation(latitude, longitude);

        findNearbyUsersWithRadius(latitude, longitude, GeoFireManager.GeoLocationRadius);

        User currentUser = ChatSDK.currentUser();
        if (currentUser != null) {
            geoFireRef().setLocation(currentUser.getEntityID(), location, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Timber.v("Location updated");
                }
            });
            // Remove the location data when we disconnect
            ref().child(currentUser.getEntityID()).onDisconnect().removeValue((databaseError, databaseReference) -> {
                Timber.v("Database listener added successfully");
            });
        }

    }

    public GeoFire geoFireRef () {
        return new GeoFire(ref());
    }

    public DatabaseReference ref () {
        return FirebasePaths.firebaseRef().child(FirebasePaths.LocationPath);
    }


    public static double distanceBetween (GeoLocation g0, GeoLocation g1) {
        // Work out the distance between the new location and the old one
        Location l0 = new Location("L0");
        l0.setLatitude(g0.latitude);
        l0.setLongitude(g0.longitude);

        Location l1 = new Location("L1");
        l1.setLatitude(g1.latitude);
        l1.setLongitude(g1.longitude);

        return l0.distanceTo(l1);

    }


}
