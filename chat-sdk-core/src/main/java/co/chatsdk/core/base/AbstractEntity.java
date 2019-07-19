package co.chatsdk.core.base;

import co.chatsdk.core.interfaces.CoreEntity;

public abstract class AbstractEntity implements CoreEntity {
    @Override
    public boolean equalsEntity(CoreEntity entity) {
        return equalsEntityID(entity.getEntityID());
    }

    @Override
    public boolean equalsEntityID(String entityID) {
        return getEntityID().equals(entityID);
    }
}
