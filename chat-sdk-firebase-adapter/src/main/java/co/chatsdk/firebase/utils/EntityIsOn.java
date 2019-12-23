package co.chatsdk.firebase.utils;

import java.util.ArrayList;

import co.chatsdk.core.base.AbstractEntity;

public class EntityIsOn {

    protected final static EntityIsOn instance = new EntityIsOn();

    public static EntityIsOn shared() {
        return instance;
    }

    ArrayList<AbstractEntity> entities = new ArrayList<>();

    public void add(AbstractEntity entity) {
        if (!contains(entity)) {
            entities.add(entity);
        }
    }

    public void remove(AbstractEntity entity) {
        AbstractEntity listEntity = entityForEntityID(entity.getEntityID());
        if (listEntity != null) {
            entities.remove(listEntity);
        }
    }

    public boolean contains(AbstractEntity entity) {
        return entityForEntityID(entity.getEntityID()) != null;
    }

    public AbstractEntity entityForEntityID(String entityID) {
        for (AbstractEntity e : entities) {
            if (e.equalsEntityID(entityID)) {
                return e;
            }
        }
        return null;
    }

}
