package co.chatsdk.core.utils;

import co.chatsdk.core.dao.User;

public interface AvatarGenerator {

    String getAvatarURL(User user);

}
