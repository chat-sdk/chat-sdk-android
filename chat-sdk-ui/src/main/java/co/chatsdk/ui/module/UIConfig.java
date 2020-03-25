package co.chatsdk.ui.module;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import co.chatsdk.ui.R;

public class UIConfig {

    /**
     * The theme to use in all activities
     */
    @StyleRes
    public int theme;

    /**
     * Default image for profile header
     */
    @DrawableRes
    public int profileHeaderImage = R.drawable.header2;

    @DrawableRes
    public int defaultProfileImage = R.drawable.icn_100_profile;

    // Rooms that are older than this will be hidden
    // Zero is infinite lifetime
    // Default - 7 days
    public int publicChatRoomLifetimeMinutes = 60 * 24 * 7;

    public boolean resetPasswordEnabled = true;

    // Message types
    public boolean imageMessagesEnabled = true;
    public boolean locationMessagesEnabled = true;

    // Chat options
    public boolean groupsEnabled = true;
    public boolean threadDetailsEnabled = true;
    public boolean publicRoomCreationEnabled = false;
    public boolean saveImagesToDirectory = false;

    public String dateFormat = "HH:mm";

    public boolean imageCroppingEnabled = true;


    public UIConfig setTheme(@StyleRes int theme) {
        this.theme = theme;
        return this;
    }

    public UIConfig setImageCroppingEnabled(boolean enabled) {
        this.imageCroppingEnabled = enabled;
        return this;
    }

    public UIConfig setResetPasswordEnabled(boolean resetPasswordEnabled) {
        this.resetPasswordEnabled = resetPasswordEnabled;
        return this;
    }

    public UIConfig setPublicRoomCreationEnabled(boolean value) {
        this.publicRoomCreationEnabled = value;
        return this;
    }

//    public UIConfig unreadMessagesCountForPublicChatRoomsEnabled(boolean value) {
//        this.unreadMessagesCountForPublicChatRoomsEnabled = value;
//        return this;
//    }

    public UIConfig setImageMessagesEnabled(boolean value) {
        this.imageMessagesEnabled = value;
        return this;
    }

    public UIConfig setLocationMessagesEnabled(boolean value) {
        this.locationMessagesEnabled = value;
        return this;
    }

    public UIConfig setGroupsEnabled(boolean value) {
        this.groupsEnabled = value;
        return this;
    }

    public UIConfig setThreadDetailsEnabled(boolean value) {
        this.threadDetailsEnabled = value;
        return this;
    }

    public UIConfig setSaveImagesToDirectoryEnabled(boolean value) {
        this.saveImagesToDirectory = value;
        return this;
    }

    public UIConfig setPublicChatRoomLifetimeMinutes (int minutes) {
        this.publicChatRoomLifetimeMinutes = minutes;
        return this;
    }

    public UIConfig setDateFormat(String format) {
        this.dateFormat = format;
        return this;
    }

    public UIConfig setSetDefaultProfileImage(@DrawableRes int res) {
        this.defaultProfileImage = res;
        return this;
    }

}
