package co.chatsdk.core.handlers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.User;

/**
 * Created by Pepe on 01/12/19.
 */

public interface ProfilePicturesHandler {

    void setGridPadding(int padding);
    void setPictureMargin(int margin);
    void setPicturesPerRow(int count);
    void setMaxPictures(int count);
    void setLimitWarning(String warning);
    void setAddButtonHidden(boolean hidden);

    void setDefaultPicture(User user, String url, List<String> urls);
    void setDefaultPicture(User user, String url);
    void addPicture(User user, String url);
    void removePicture(User user, String url);
    void replacePicture(User user, String prevUrl, String newUrl);
    void setPictures(User user, List<String> urls);
    ArrayList<String> fromUser(User user);

    Class getProfilePicturesActivity();
    void startProfilePicturesActivity(Context context, String userEntityID);

}
