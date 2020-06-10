package sdk.chat.core.avatar;

import sdk.chat.core.dao.User;

public interface AvatarGenerator {
    String getAvatarURL(User user);
}
