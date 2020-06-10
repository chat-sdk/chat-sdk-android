package sdk.chat.core.interfaces;

/**
 * Created by ben on 10/9/17.
 */

public interface UserListItem extends Entity {

    String getName();
    String getStatus();
    String getAvailability();
    String getAvatarURL();
    Boolean getIsOnline();

}

