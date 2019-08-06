package co.chatsdk.core.dao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import co.chatsdk.core.base.AbstractEntity;
import co.chatsdk.core.interfaces.CoreEntity;

@Entity
public class MessageMetaValue extends AbstractEntity implements MetaValue {

    @Id
    private Long id;

    private String key;
    private String value;

    private Long messageId;

    @ToOne(joinProperty = "messageId")
    private Message message;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1491679537)
    private transient MessageMetaValueDao myDao;

    @Generated(hash = 304350448)
    public MessageMetaValue(Long id, String key, String value, Long messageId) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.messageId = messageId;
    }

    @Generated(hash = 739600636)
    public MessageMetaValue() {
    }

    @Generated(hash = 1728529602)
    private transient Long message__resolvedKey;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setEntityID(String entityID) {

    }

    @Override
    public String getEntityID() {
        return null;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return this.messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1905538284)
    public Message getMessage() {
        Long __key = this.messageId;
        if (message__resolvedKey == null || !message__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MessageDao targetDao = daoSession.getMessageDao();
            Message messageNew = targetDao.load(__key);
            synchronized (this) {
                message = messageNew;
                message__resolvedKey = __key;
            }
        }
        return message;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 770507318)
    public void setMessage(Message message) {
        synchronized (this) {
            this.message = message;
            messageId = message == null ? null : message.getId();
            message__resolvedKey = messageId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 371282140)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMessageMetaValueDao() : null;
    }
}
