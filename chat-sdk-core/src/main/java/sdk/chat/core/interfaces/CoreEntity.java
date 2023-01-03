package sdk.chat.core.interfaces;

import sdk.chat.core.dao.Updatable;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public interface CoreEntity extends Entity, Updatable {

    void setEntityID (String entityID);
    boolean equalsEntity(CoreEntity entity);
    boolean equalsEntityID(String entityID);
    void update();

}
