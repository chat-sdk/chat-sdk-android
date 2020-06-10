package sdk.chat.core.api;

import io.reactivex.Completable;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;

public class SimpleAPI {

    public static Completable updateUserAsync(String name, String avatarURL) {
        User currentUser = ChatSDK.core().currentUser();

        currentUser.setName(name);
        currentUser.setAvatarURL(avatarURL);

        return ChatSDK.core().pushUser();
    }

    public static void updateUser(String name, String avatarURL) {
        updateUserAsync(name, avatarURL).subscribe();
    }

}
