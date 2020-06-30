package sdk.chat.message.location;

import android.graphics.drawable.Drawable;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.options.BaseChatOption;

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