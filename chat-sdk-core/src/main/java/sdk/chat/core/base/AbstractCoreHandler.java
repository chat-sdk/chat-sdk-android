package sdk.chat.core.base;

import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.CoreHandler;
import sdk.chat.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractCoreHandler implements CoreHandler {

    private User cachedUser = null;

    @Deprecated
    public User currentUserModel(){
        return currentUser();
    }

    @Override
    public User currentUser() {
        String entityID = ChatSDK.auth().getCurrentUserEntityID();

        if(cachedUser == null || !cachedUser.equalsEntityID(entityID)) {
            if (entityID != null && !entityID.isEmpty()) {
                cachedUser = DaoCore.fetchEntityWithEntityID(User.class, entityID);
            }
            else {
                cachedUser = null;
            }
        }
        return cachedUser;
    }

    @Override
    public void goOnline() {
        if (ChatSDK.lastOnline() != null) {
            ChatSDK.lastOnline().setLastOnline(currentUser());
        }
    }
}
