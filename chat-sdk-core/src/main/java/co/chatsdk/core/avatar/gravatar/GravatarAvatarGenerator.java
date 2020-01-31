package co.chatsdk.core.avatar;

import co.chatsdk.core.dao.User;

// TODO: https://github.com/rynr/gravatar
public class GravatarAvatarGenerator implements AvatarGenerator {
    @Override
    public String getAvatarURL(User user) {
        return new FlatHashAvatarGenerator().getAvatarURL(user);
    }
}
