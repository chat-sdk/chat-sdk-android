package sdk.chat.core.interfaces;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public interface CoreEntity extends Entity {

    void setEntityID (String entityID);
    boolean equalsEntity(CoreEntity entity);
    boolean equalsEntityID(String entityID);

}
