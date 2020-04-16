package co.chatsdk.core.interfaces;

/**
 * Created by ben on 10/9/17.
 */

public interface UserListItem extends CoreEntity {

    String getName();
    String getStatus();
    String getAvailability();
    String getAvatarURL();
    Boolean getIsOnline();

}

