package co.chatsdk.core.avatar.gravatar;

import co.chatsdk.core.avatar.AvatarGenerator;
import co.chatsdk.core.avatar.FlatHashAvatarGenerator;
import co.chatsdk.core.dao.User;

// TODO: https://github.com/rynr/gravatar
public class GravatarAvatarGenerator extends FlatHashAvatarGenerator {
    @Override
    public String getAvatarURL(User user) {
        String url = new FlatHashAvatarGenerator().getAvatarURL(user);
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            url = Gravatar.init().with(user.getEmail()).force404().build();
        }
        return url;
    }
}
