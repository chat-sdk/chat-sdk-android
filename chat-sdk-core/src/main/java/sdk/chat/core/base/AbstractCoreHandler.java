package sdk.chat.core.base;

import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.CoreHandler;
import sdk.chat.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractCoreHandler implements CoreHandler {

    @Deprecated
    public User currentUserModel(){
        return currentUser();
    }

    @Override
    public User currentUser() {
        return ChatSDK.auth().currentUser();
    }

}
