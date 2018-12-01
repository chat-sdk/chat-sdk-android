package co.chatsdk.profile.pictures;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.ProfilePicturesHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.utils.HashMapHelper;

/**
 * Created by Pepe on 01/12/19.
 */

public class BaseProfilePicturesHandler implements ProfilePicturesHandler {

    public static final String PictureURLS = "pictures";

    private HashMap<String, Object> expandMeta(Map<String, String> meta) {
        return HashMapHelper.expand((HashMap<String, String>) meta);
    }

    private HashMap<String, String> flattenMeta(Map<String, Object> meta) {
        meta = HashMapHelper.flatten(meta);
        HashMap<String, String> flatMeta = new HashMap<>();
        for (Map.Entry<String, Object> entry : meta.entrySet()) {
            if (entry.getValue() instanceof String) {
                flatMeta.put(entry.getKey(), (String) entry.getValue());
            }
        }
        return flatMeta;
    }

    private ArrayList<String> addDefaultAvatar(User user, List<String> urls) {
        String avatarURL = user.getAvatarURL();
        if (avatarURL != null && !avatarURL.isEmpty() && urls.indexOf(avatarURL) < 0) {
            urls.add(0, avatarURL);
        }
        return (ArrayList<String>) urls;
    }

    public void setDefaultPicture(User user, String url, List<String> urls) {
        if (urls.size() > 1) {
            if (urls.indexOf(url) > -1) {
                urls.remove(url);
            }
            urls.add(0, url);
            setPictures(user, urls);
        }
        user.setAvatarURL(url);
    }

    public void setDefaultPicture(User user, String url) {
        setDefaultPicture(user, url, fromUser(user));
    }

    public void addPicture(User user, String url) {
        ArrayList<String> urls = fromUser(user);
        if (urls.size() == 0) {
            user.setAvatarURL(url);
        } else {
            urls.add(url);
            setPictures(user, urls);
        }

    }

    public void removePicture(User user, String url) {
        ArrayList<String> urls = fromUser(user);
        boolean isDefault = urls.indexOf(url) == 0;
        urls.remove(url);
        urls.add("");
        if (isDefault) {
            setDefaultPicture(user, urls.get(0), urls);
        }
        setPictures(user, urls);
    }

    public void replacePicture(User user, String prevUrl, String newUrl) {
        ArrayList<String> urls = fromUser(user);
        int i = urls.indexOf(prevUrl);
        if (i > -1) {
            urls.set(i, newUrl);
            setPictures(user, urls);
        }
    }

    public void setPictures(User user, List<String> urls) {
        HashMap<String, String> pictures = new HashMap<>();
        for (int i = 0; i < urls.size(); i++) {
            pictures.put(Integer.toString(i), urls.get(i));
        }
        Map<String, Object> expendedMeta = expandMeta(user.metaMap());
        expendedMeta.put(PictureURLS, pictures);
        Map<String, String> meta = flattenMeta(expendedMeta);
        user.setMetaMap(meta);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> fromUser(User user) {
        Map<String, Object> expandedMeta = expandMeta(user.metaMap());
        Object picturesData = expandedMeta.get(PictureURLS);
        if (picturesData instanceof HashMap) {
            Map<String, String> picturesMap = (HashMap<String, String>) picturesData;
            ArrayList<String> urls = new ArrayList<>(picturesMap.values().size());
            for (String url : picturesMap.values()) {
                if (url != null && !url.isEmpty()) {
                    urls.add(url);
                }
            }
            return addDefaultAvatar(user, urls);
        } else {
            return addDefaultAvatar(user, new ArrayList<>());
        }
    }

    public Class getProfilePicturesActivity() {
        return ProfilePicturesActivity.class;
    }

    public void startProfilePicturesActivity(Context context, String userEntityID) {
        Intent intent = new Intent(context, getProfilePicturesActivity());
        intent.putExtra(InterfaceManager.USER_ENTITY_ID, userEntityID);
        ChatSDK.ui().startActivity(context, intent);
    }

}
