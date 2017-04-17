package wanderingdevelopment.tk.chatsdkcore.entities;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.DateTime;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import wanderingdevelopment.tk.chatsdkcore.db.DaoSession;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;
import org.greenrobot.greendao.annotation.NotNull;
import wanderingdevelopment.tk.chatsdkcore.db.MessageDao;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;

/**
 * Created by kykrueger on 2016-10-23.
 */

@Entity
public class Message {
    @Id
    private Long id;
    private Long threadId;
    @Convert(converter = TypeConverter.class, columnType = Integer.class)
    private Type type;
    @Convert(converter = DateTimeConverter.class, columnType = Long.class)
    private DateTime dateTime;
    private String text;
    private String imagePayload;
    private long senderId;
    @ToOne(joinProperty = "senderId")
    private User sender;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 859287859)
    private transient MessageDao myDao;
    @Generated(hash = 880682693)
    private transient Long sender__resolvedKey;

    @Generated(hash = 161755879)
    public Message(Long id, Long threadId, Type type, DateTime dateTime, String text,
            String imagePayload, long senderId) {
        this.id = id;
        this.threadId = threadId;
        this.type = type;
        this.dateTime = dateTime;
        this.text = text;
        this.imagePayload = imagePayload;
        this.senderId = senderId;
    }

    @Generated(hash = 637306882)
    public Message() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return this.threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public DateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePayload() {
        return this.imagePayload;
    }

    public void setImagePayload(String imagePayload) {
        this.imagePayload = imagePayload;
    }

    public long getSenderId() {
        return this.senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 717646372)
    public User getSender() {
        long __key = this.senderId;
        if (sender__resolvedKey == null || !sender__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            User senderNew = targetDao.load(__key);
            synchronized (this) {
                sender = senderNew;
                sender__resolvedKey = __key;
            }
        }
        return sender;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1908446527)
    public void setSender(@NotNull User sender) {
        if (sender == null) {
            throw new DaoException(
                    "To-one property 'senderId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.sender = sender;
            senderId = sender.getId();
            sender__resolvedKey = senderId;
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
    @Generated(hash = 747015224)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMessageDao() : null;
    }

    /***
     *  Custom class converters start here
     */
    public enum Type {
        DEFAULT(0), IMAGE(1), LOCATION(2);

        final int id;

        Type(int id) {
            this.id = id;
        }
    }

    public static class TypeConverter implements PropertyConverter<Type, Integer> {
        @Override
        public Type convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (Type type : Type.values()) {
                if (type.id == databaseValue) {
                    return type;
                }
            }
            return Type.DEFAULT;
        }

        @Override
        public Integer convertToDatabaseValue(Type entityProperty) {
            return entityProperty == null ? null : entityProperty.id;
        }
    }

    // TODO: test how this handles timezones
    public static class DateTimeConverter implements PropertyConverter<DateTime, Long> {
        @Override
        public DateTime convertToEntityProperty(Long databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            return new DateTime(databaseValue);
        }

        @Override
        public Long convertToDatabaseValue(DateTime dateTime) {
            return dateTime == null ? null : dateTime.getMillis();
        }
    }
}
