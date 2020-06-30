package sdk.chat.message.location;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.module.UIModule;

public class LocationMessageModule extends AbstractModule {

    public static final LocationMessageModule instance = new LocationMessageModule();

    public static LocationMessageModule shared() {
        return instance;
    }


    protected LocationProvider locationProvider;

    @Override
    public void activate(@NonNull Context context) throws Exception {
        locationProvider = new LocationProvider();
        if(UIModule.config().locationMessagesEnabled) {
            ChatSDK.ui().addChatOption(new LocationChatOption(context.getResources().getString(sdk.chat.ui.R.string.location)));
        }
    }

    @Override
    public String getName() {
        return "LocationMessageModule";
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public List<String> requiredPermissions() {
        return new ArrayList<String>() {{
            add(Manifest.permission.ACCESS_COARSE_LOCATION);
            add(Manifest.permission.ACCESS_FINE_LOCATION);
        }};
    }

}

