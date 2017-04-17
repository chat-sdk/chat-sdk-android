package wanderingdevelopment.tk.chatsdkcore.entities;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import wanderingdevelopment.tk.chatsdkcore.db.DaoSession;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;
import wanderingdevelopment.tk.chatsdkcore.db.MessageDao;
import wanderingdevelopment.tk.chatsdkcore.db.ThreadDao;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;

/**
 * Created by kykrueger on 2016-10-22.
 */

@Entity
public class Thread {

    @Id
    private Long id;
    @Convert(converter = TypeConverter.class, columnType = Integer.class)
    private Type type;
    private String name;

    @ToMany(referencedJoinProperty = "threadId")
    @OrderBy("dateTime ASC")
    private List<Message> messages;

    @ToMany
    @JoinEntity(
            entity = JoinThreadWithUser.class,
            sourceProperty = "threadId",
            targetProperty = "userId"
    )
    private List<User> usersInConversation;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 473811190)
    private transient ThreadDao myDao;

    @Generated(hash = 1268136860)
    public Thread(Long id, Type type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    @Generated(hash = 218849146)
    public Thread() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 581641122)
    public List<Message> getMessages() {
        if (messages == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MessageDao targetDao = daoSession.getMessageDao();
            List<Message> messagesNew = targetDao._queryThread_Messages(id);
            synchronized (this) {
                if (messages == null) {
                    messages = messagesNew;
                }
            }
        }
        return messages;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1942469556)
    public synchronized void resetMessages() {
        messages = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 710407001)
    public List<User> getUsersInConversation() {
        if (usersInConversation == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            List<User> usersInConversationNew = targetDao._queryThread_UsersInConversation(id);
            synchronized (this) {
                if (usersInConversation == null) {
                    usersInConversation = usersInConversationNew;
                }
            }
        }
        return usersInConversation;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 221367967)
    public synchronized void resetUsersInConversation() {
        usersInConversation = null;
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
    @Generated(hash = 5320433)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getThreadDao() : null;
    }

    /***
     *  Custom class converters start here
     */
    public enum Type {
        SINGLE(0), MULTI(1);

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
            return Type.SINGLE;
        }

        @Override
        public Integer convertToDatabaseValue(Type entityProperty) {
            return entityProperty == null ? null : entityProperty.id;
        }
    }
}
