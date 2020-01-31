package co.chatsdk.core.avatar;

import co.chatsdk.core.dao.User;

public interface AvatarGenerator {
    String getAvatarURL(User user);
}
