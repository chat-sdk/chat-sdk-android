package tk.wanderingdevelopment.chatsdk.core.interfaces;

import android.support.annotation.StringRes;

import com.github.johnpersano.supertoasts.SuperToast;

/**
 * Created by KyleKrueger on 14.04.2017.
 */

public interface UiLauncherInterface {
    void startChatActivityForID(long id);

    void startLoginActivity(boolean loggedOut);

    void startMainActivity();

    void startSearchActivity();

    void startPickFriendsActivity();

    void startShareWithFriendsActivity();

    void startShareLocationActivityActivity();

    boolean startProfileActivity(String entityId);

    boolean startProfileActivity(long id);

    void startEditProfileActivity(long id);

    void initCardToast();

    void dismissProgressCard();

    void dismissProgressCardWithSmallDelay();

    void dismissProgressCard(long delay);

    void showProgressCard(String text);

    void showProgressCard(@StringRes int resourceId);

    /*Getters and Setters*/
    void showToast(String text);

    void showToast(@StringRes int resourceId);


    void setAlertToast(SuperToast alertToast);

    void setToast(SuperToast toast);

    SuperToast getAlertToast();

    SuperToast getToast();

    void setSearchActivity(Class searchActivity);

    Class getSearchActivity();

    Class getShareLocationActivity();

    Class getThreadDetailsActivity();

    Class getPickFriendsActivity();

    void setPickFriendsActivity(Class pickFriendsActivity);

    void setShareWithFriendsActivity(Class shareWithFriendsActivity);

    void setShareLocationActivity(Class shareLocationActivity);

    void setProfileActivity(Class profileActivity);

    Class getLoginActivity();

    Class getMainActivity();

    Class getChatActivity();
}
