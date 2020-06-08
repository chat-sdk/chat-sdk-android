package sdk.chat.core.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class UserThreadLinkMetaValue implements MetaValue<String> {

    @Id private Long id;

    private String key;
    private String value;

    private Long userThreadLinkId;

    @ToOne(joinProperty = "userThreadLinkId")
    private UserThreadLink userThreadLink;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 421818365)
    private transient UserThreadLinkMetaValueDao myDao;

    @Generated(hash = 1472740785)
    public UserThreadLinkMetaValue(Long id, String key, String value,
            Long userThreadLinkId) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.userThreadLinkId = userThreadLinkId;
    }

    @Generated(hash = 1776174827)
    public UserThreadLinkMetaValue() {
    }

    @Generated(hash = 1100632231)
    private transient Long userThreadLink__resolvedKey;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserThreadLinkId() {
        return this.userThreadLinkId;
    }

    public void setUserThreadLinkId(Long userThreadLinkId) {
        this.userThreadLinkId = userThreadLinkId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 644626697)
    public UserThreadLink getUserThreadLink() {
        Long __key = this.userThreadLinkId;
        if (userThreadLink__resolvedKey == null
                || !userThreadLink__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserThreadLinkDao targetDao = daoSession.getUserThreadLinkDao();
            UserThreadLink userThreadLinkNew = targetDao.load(__key);
            synchronized (this) {
                userThreadLink = userThreadLinkNew;
                userThreadLink__resolvedKey = __key;
            }
        }
        return userThreadLink;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1883455290)
    public void setUserThreadLink(UserThreadLink userThreadLink) {
        synchronized (this) {
            this.userThreadLink = userThreadLink;
            userThreadLinkId = userThreadLink == null ? null
                    : userThreadLink.getId();
            userThreadLink__resolvedKey = userThreadLinkId;
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
    @Generated(hash = 92934671)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserThreadLinkMetaValueDao()
                : null;
    }

}
