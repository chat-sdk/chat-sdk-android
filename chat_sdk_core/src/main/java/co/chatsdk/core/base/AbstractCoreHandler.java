package co.chatsdk.core.base;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.handlers.CoreHandler;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractCoreHandler implements CoreHandler {

    public BUser currentUserModel(){
        String entityID = NM.auth().getCurrentUserEntityID();

        if (StringUtils.isNotEmpty(entityID))
        {
            BUser currentUser = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

            return currentUser;
        }
        return null;
    }

}
