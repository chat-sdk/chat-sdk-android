package co.chatsdk.core;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.interfaces.CoreEntity;
import co.chatsdk.core.interfaces.StorageAdapter;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class StorageManager {

    private static StorageManager instance;
    public StorageAdapter a;

    public static StorageManager shared(){
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

    public <T extends CoreEntity> T fetchOrCreateEntityWithEntityID(Class<T> c, String entityId){

        T entity = DaoCore.fetchEntityWithEntityID(c, entityId);
//
        if (entity == null)
        {
            entity = DaoCore.getEntityForClass(c);

            if(entityId instanceof String) {
                entity.setEntityID((String) entityId);
            }
            else {
                entity.setEntityID(entityId.toString());
                Timber.v("ERROR!!! The entity must always be a string");
            }

            entity = DaoCore.createEntity(entity);
        }

        return entity;
    }

}
