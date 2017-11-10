package co.chatsdk.core.dao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;
import org.joda.time.DateTime;

import co.chatsdk.core.interfaces.CoreEntity;
import co.chatsdk.core.utils.DaoDateTimeConverter;

/**
 * Created by ben on 10/5/17.
 */

@Entity
public class ReadReceiptUserLink implements CoreEntity {

    @Id
    private long id;

    private long readReceiptId;
    private long userId;
    private int status;

    @Convert(converter = DaoDateTimeConverter.class, columnType = Long.class)
    private DateTime date;

    @ToOne(joinProperty = "userId")
    private User user;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 642681985)
    private transient ReadReceiptUserLinkDao myDao;

    @Generated(hash = 305755340)
    public ReadReceiptUserLink(long id, long readReceiptId, long userId, int status, DateTime date) {
        this.id = id;
        this.readReceiptId = readReceiptId;
        this.userId = userId;
        this.status = status;
        this.date = date;
    }

    @Generated(hash = 1437583804)
    public ReadReceiptUserLink() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReadReceiptId() {
        return this.readReceiptId;
    }

    public void setReadReceiptId(long readReceiptId) {
        this.readReceiptId = readReceiptId;
    }

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setEntityID(String entityID) {
        setId(Long.parseLong(entityID));
    }

    @Override
    public String getEntityID() {
        return Long.toString(id);
    }

    @Generated(hash = 251390918)
    private transient Long user__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 115391908)
    public User getUser() {
        long __key = this.userId;
        if (user__resolvedKey == null || !user__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            User userNew = targetDao.load(__key);
            synchronized (this) {
                user = userNew;
                user__resolvedKey = __key;
            }
        }
        return user;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 462495677)
    public void setUser(@NotNull User user) {
        if (user == null) {
            throw new DaoException(
                    "To-one property 'userId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.user = user;
            userId = user.getId();
            user__resolvedKey = userId;
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
    @Generated(hash = 1250799234)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getReadReceiptUserLinkDao() : null;
    }

    public DateTime getDate() {
        return this.date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }


}
