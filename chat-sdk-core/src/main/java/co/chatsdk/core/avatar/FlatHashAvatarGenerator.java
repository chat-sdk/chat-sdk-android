package co.chatsdk.core.avatar;

import co.chatsdk.core.dao.User;

public class RobohashAvatarGenerator implements AvatarGenerator {
    @Override
    public String getAvatarURL(User user) {
        return "http://flathash.com/" + user.getEntityID();
    }
}
