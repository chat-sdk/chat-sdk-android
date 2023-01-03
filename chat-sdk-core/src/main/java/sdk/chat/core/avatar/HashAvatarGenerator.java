package sdk.chat.core.avatar;

import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;

public class HashAvatarGenerator implements AvatarGenerator {

    @Override
    public String getAvatarURL(User user) {
        if (ChatSDK.config().identiconBaseURL == null || user == null || user.getEntityID() == null) {
            return null;
        }
        return String.format(ChatSDK.config().identiconBaseURL, user.getEntityID());
    }


}
