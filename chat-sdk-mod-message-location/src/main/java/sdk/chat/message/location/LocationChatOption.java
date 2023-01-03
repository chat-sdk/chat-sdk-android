package sdk.chat.message.location;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.options.BaseChatOption;

/**
 * Created by ben on 10/11/17.
 */

public class LocationChatOption extends BaseChatOption {
    public LocationChatOption(@StringRes int title, @DrawableRes int image) {
        super(title, image, (activity, launcher, thread) -> {
            return new LocationSelector().startChooseLocationActivity(activity).flatMapCompletable(result -> ChatSDK.locationMessage().sendMessageWithLocation(result.snapshotPath, result.latLng, thread));
        });
    }
}