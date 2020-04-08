package co.chatsdk.ui.chat.options;

import android.graphics.drawable.Drawable;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.chat.LocationSelector;

/**
 * Created by ben on 10/11/17.
 */

public class LocationChatOption extends BaseChatOption {

    public LocationChatOption(String title, Drawable iconDrawable) {
        super(title, iconDrawable, (activity, thread) -> {
            return new LocationSelector().startChooseLocationActivity(activity).flatMapCompletable(result -> ChatSDK.locationMessage().sendMessageWithLocation(result.snapshotPath, result.latLng, thread));
        });
    }

    public LocationChatOption(String title) {
        this(title, null);
    }
}