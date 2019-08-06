package co.chatsdk.core.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import co.chatsdk.core.base.AbstractEntity;
import co.chatsdk.core.interfaces.CoreEntity;

/**
 * Created by ben on 5/16/18.
 */

@Entity
public class ThreadMetaValue extends AbstractEntity implements MetaValue {

    @Id
    private Long id;

    private String key;
    private String value;

    private Long threadId;

    @ToOne(joinProperty = "threadId")
    private Thread thread;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 3171812)
    private transient ThreadMetaValueDao myDao;

    @Generated(hash = 965961666)
    public ThreadMetaValue(Long id, String key, String value, Long threadId) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.threadId = threadId;
    }

    @Generated(hash = 1556136064)
    public ThreadMetaValue() {
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

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getThreadId() {
        return this.threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    @Generated(hash = 1974258785)
    private transient Long thread__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1483947909)
    public Thread getThread() {
        Long __key = this.threadId;
        if (thread__resolvedKey == null || !thread__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ThreadDao targetDao = daoSession.getThreadDao();
            Thread threadNew = targetDao.load(__key);
            synchronized (this) {
                thread = threadNew;
                thread__resolvedKey = __key;
            }
        }
        return thread;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1938921797)
    public void setThread(Thread thread) {
        synchronized (this) {
            this.thread = thread;
            threadId = thread == null ? null : thread.getId();
            thread__resolvedKey = threadId;
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
    @Generated(hash = 720159862)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getThreadMetaValueDao() : null;
    }


    @Override
    public void setEntityID(String entityID) {
    }

    @Override
    public String getEntityID() {
        return null;
    }
}
