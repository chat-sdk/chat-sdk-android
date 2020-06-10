package sdk.chat.core.avatar.gravatar;

import sdk.chat.core.avatar.HashAvatarGenerator;
import sdk.chat.core.dao.User;

// TODO: https://github.com/rynr/gravatar
public class GravatarAvatarGenerator extends HashAvatarGenerator {
    @Override
    public String getAvatarURL(User user) {
        String url = new HashAvatarGenerator().getAvatarURL(user);
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            url = Gravatar.init().with(user.getEmail()).force404().build();
        }
        return url;
    }
}
