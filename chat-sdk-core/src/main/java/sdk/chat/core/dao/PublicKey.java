package sdk.chat.core.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import sdk.chat.core.base.AbstractEntity;

@Entity
public class PublicKey extends AbstractEntity {

    @Id
    private Long id;

    @Unique
    private String entityID;

    private String key;
    private String identifier;

    @Generated(hash = 981958860)
    public PublicKey(Long id, String entityID, String key, String identifier) {
        this.id = id;
        this.entityID = entityID;
        this.key = key;
        this.identifier = identifier;
    }

    @Generated(hash = 285518041)
    public PublicKey() {
    }

    @Override
    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    @Override
    public String getEntityID() {
        return entityID;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
