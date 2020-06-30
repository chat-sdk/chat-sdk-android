package sdk.chat.ui.module;

import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.ui.BaseInterfaceAdapter;
import sdk.chat.ui.R;
import sdk.guru.common.BaseConfig;

public class UIConfig<T> extends BaseConfig<T> {

    /**
     * The theme to use in all activities
     */
    @StyleRes
    public int theme = R.style.ChatSDKTheme;

    /**
     * Default image for profile header
     */
    @DrawableRes
    public int profileHeaderImage = R.drawable.header;

    @DrawableRes
    public int defaultProfilePlaceholder = R.drawable.icn_100_profile;

    public boolean resetPasswordEnabled = true;

    // Message types
    public boolean imageMessagesEnabled = true;
    public boolean locationMessagesEnabled = true;

    // Chat options
    public boolean groupsEnabled = true;
    public boolean threadDetailsEnabled = true;
    public boolean publicRoomCreationEnabled = false;
    public boolean publicRoomRoomsEnabled = true;
    public boolean saveImagesToDirectory = false;
    public boolean requestPermissionsOnStartup = true;
    public boolean showNamesInGroupChatView = true;
    public boolean showAvatarInChatView = true;

    public String dateFormat = "HH:mm";

    public String usernameHint = null;

    public boolean imageCroppingEnabled = true;

    public boolean allowBackPressFromMainActivity = false;

    public Class<? extends InterfaceAdapter> interfaceAdapter = BaseInterfaceAdapter.class;

    public UIConfig(T onBuild) {
        super(onBuild);
    }

    /**
     * Customize the app theme
     * @param theme
     * @return
     */
    public UIConfig<T> setTheme(@StyleRes int theme) {
        this.theme = theme;
        return this;
    }

    public UIConfig<T> overrideTheme() {
        this.theme = 0;
        return this;
    }

    /**
     * Enable the image cropper
     * @param enabled
     * @return
     */
    public UIConfig<T> setImageCroppingEnabled(boolean enabled) {
        this.imageCroppingEnabled = enabled;
        return this;
    }

    /**
     * Show the reset password button
     * @param resetPasswordEnabled
     * @return
     */
    public UIConfig<T> setResetPasswordEnabled(boolean resetPasswordEnabled) {
        this.resetPasswordEnabled = resetPasswordEnabled;
        return this;
    }

    /**
     * Allow users to create public chat rooms
     * @param value
     * @return
     */
    public UIConfig<T> setPublicRoomCreationEnabled(boolean value) {
        this.publicRoomCreationEnabled = value;
        return this;
    }

    /**
     * Enable public chat rooms
     * @param value
     * @return
     */
    public UIConfig<T> setPublicRoomsEnabled(boolean value) {
        this.publicRoomRoomsEnabled = value;
        return this;
    }

    /**
     * Enable image messages
     * @param value
     * @return
     */
    public UIConfig<T> setImageMessagesEnabled(boolean value) {
        this.imageMessagesEnabled = value;
        return this;
    }

    /**
     * Enable location messages
     * @param value
     * @return
     */
    public UIConfig<T> setLocationMessagesEnabled(boolean value) {
        this.locationMessagesEnabled = value;
        return this;
    }

    /**
     * Allow group chats
     * @param value
     * @return
     */
    public UIConfig<T> setGroupsEnabled(boolean value) {
        this.groupsEnabled = value;
        return this;
    }

    /**
     * Allow the user to see thread details screen
     * @param value
     * @return
     */
    public UIConfig<T> setThreadDetailsEnabled(boolean value) {
        this.threadDetailsEnabled = value;
        return this;
    }

    /**
     * Automatically save images to directory
     * @param value
     * @return
     */
    public UIConfig<T> setSaveImagesToDirectoryEnabled(boolean value) {
        this.saveImagesToDirectory = value;
        return this;
    }

    /**
     * Date format used for Chat Activity
     * default is yyyy-MM-dd HH:mm:ss
     * @param format
     * @return
     */
    public UIConfig<T> setDateFormat(String format) {
        this.dateFormat = format;
        return this;
    }

    /**
     * Default profile image resource
     * @param res
     * @return
     */
    public UIConfig<T> setDefaultProfilePlaceholder(@DrawableRes int res) {
        this.defaultProfilePlaceholder = res;
        return this;
    }

    /**
     * Default profile header image
     * @param profileHeaderImage
     * @return
     */
    public UIConfig<T> setProfileHeaderImage(int profileHeaderImage) {
        this.profileHeaderImage = profileHeaderImage;
        return this;
    }

    /**
     * Hint used on Login screen for username
     * @param usernameHint
     * @return
     */
    public UIConfig<T> setUsernameHint(String usernameHint) {
        this.usernameHint = usernameHint;
        return this;
    }

    /**
     * If this is set to true, then when a user clicks back from the main
     * activity, the activity will close. This should be used if you are
     * launching the ChatSDK from your app
     *
     * Enabled:
     *
     * 1. User clicks button in your app which launches Chat SDK main activity
     * 2. User clicks back, is returned to host app
     *
     * @param allow user to dismiss main activity
     * @return config
     */
    public UIConfig<T> setAllowBackPressFromMainActivity(boolean allow) {
        this.allowBackPressFromMainActivity = allow;
        return this;
    }

    /**
     * Override the interface adapter
     * @param interfaceAdapter
     * @return
     */
    public UIConfig<T> setInterfaceAdapter(Class<? extends InterfaceAdapter> interfaceAdapter) {
        this.interfaceAdapter = interfaceAdapter;
        return this;
    }

    /**
     * Request all permissions when the app starts up
     * @param request
     * @return
     */
    public UIConfig<T> setRequestPermissionsOnStartup(boolean request) {
        this.requestPermissionsOnStartup = request;
        return this;
    }

    /**
     * Show user names in the public / private group chat view
     * @param request
     * @return
     */
    public UIConfig<T> setShowNamesInGroupChatView(boolean request) {
        this.showNamesInGroupChatView = request;
        return this;
    }

    /**
     * Show user avatar in chat view
     * @param request
     * @return
     */
    public UIConfig<T> setShowAvatarInChatView(boolean request) {
        this.showAvatarInChatView = request;
        return this;
    }
}
