package co.chatsdk.core.base;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.handlers.CoreHandler;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractCoreHandler implements CoreHandler {

    private User cachedUser = null;

    public User currentUserModel(){
        String entityID = NM.auth().getCurrentUserEntityID();

        if(cachedUser == null || !cachedUser.getEntityID().equals(entityID)) {
            if (StringUtils.isNotEmpty(entityID)) {
                cachedUser = DaoCore.fetchEntityWithEntityID(User.class, entityID);
            }
        }
       return cachedUser;
    }

}
