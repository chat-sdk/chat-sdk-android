package sdk.chat.core.dao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Created by ben on 5/16/18.
 */

@Entity
public class ThreadMetaValue implements MetaValue<Object> {

    @Id
    private Long id;

    private String key;
    private String stringValue;
    private Boolean booleanValue;
    private Integer integerValue;
    private Long longValue;
    private Float floatValue;

    private Long threadId;

    @ToOne(joinProperty = "threadId")
    private Thread thread;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 3171812)
    private transient ThreadMetaValueDao myDao;

    @Generated(hash = 770896123)
    public ThreadMetaValue(Long id, String key, String stringValue, Boolean booleanValue,
            Integer integerValue, Long longValue, Float floatValue, Long threadId) {
        this.id = id;
        this.key = key;
        this.stringValue = stringValue;
        this.booleanValue = booleanValue;
        this.integerValue = integerValue;
        this.longValue = longValue;
        this.floatValue = floatValue;
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

    @Override
    public void setValue(Object value) {
        stringValue = null;
        integerValue = null;
        longValue = null;
        booleanValue = null;
        floatValue = null;
        if (value instanceof String) {
            setStringValue((String) value);
        }
        if (value instanceof Integer) {
            setIntegerValue((Integer) value);
        }
        if (value instanceof Long) {
            setLongValue((Long) value);
        }
        if (value instanceof Boolean) {
            setBooleanValue((Boolean) value);
        }
        if (value instanceof Float) {
            setFloatValue((Float) value);
        }
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

    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Integer getIntegerValue() {
        return this.integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Long getLongValue() {
        return this.longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Float getFloatValue() {
        return this.floatValue;
    }

    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Object getValue() {
        if (getStringValue() != null) {
            return getStringValue();
        }
        if (getIntegerValue() != null) {
            return getIntegerValue();
        }
        if (getLongValue() != null) {
            return getLongValue();
        }
        if (getBooleanValue() != null) {
            return getBooleanValue();
        }
        if (getFloatValue() != null) {
            return getFloatValue();
        }
        return null;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

}
