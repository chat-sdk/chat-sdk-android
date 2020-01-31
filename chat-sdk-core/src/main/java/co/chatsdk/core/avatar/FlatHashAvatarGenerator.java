package co.chatsdk.core.avatar;

import java.util.Random;

import co.chatsdk.core.dao.User;

public class FlatHashAvatarGenerator implements AvatarGenerator {

    public static String baseURL = "http://flathash.com/";

    @Override
    public String getAvatarURL(User user) {
        String hash = String.valueOf(new Random().nextInt(1000));
        if (user != null && user.getEntityID() != null) {
            hash = user.getName();
        }
        if (user != null) {
            hash = user.getEntityID();
        }
        return baseURL = baseURL + hash + ".png";
    }

}
