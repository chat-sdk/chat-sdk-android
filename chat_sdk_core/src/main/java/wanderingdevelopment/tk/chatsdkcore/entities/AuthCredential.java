package wanderingdevelopment.tk.chatsdkcore.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;
import wanderingdevelopment.tk.chatsdkcore.db.DaoSession;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;
import org.greenrobot.greendao.annotation.NotNull;
import wanderingdevelopment.tk.chatsdkcore.db.AuthCredentialDao;
import wanderingdevelopment.tk.chatsdkcore.entities.User;
import wanderingdevelopment.tk.chatsdkcore.entities.User;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;

/**
 * Created by kykrueger on 2016-12-03.
 */

@Entity
public class AuthCredential {

    private String userAlias;
    private String userPassword;

    private long currentUserId;

    @ToOne(joinProperty = "currentUserId")
    private User currentUser;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 245867060)
    private transient AuthCredentialDao myDao;
    @Generated(hash = 942731541)
    private transient Long currentUser__resolvedKey;

    @Generated(hash = 1926045155)
    public AuthCredential(String userAlias, String userPassword, long currentUserId) {
        this.userAlias = userAlias;
        this.userPassword = userPassword;
        this.currentUserId = currentUserId;
    }
    @Generated(hash = 1621553665)
    public AuthCredential() {
    }
    public String getUserAlias() {
        return this.userAlias;
    }
    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }
    public String getUserPassword() {
        return this.userPassword;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public long getCurrentUserId() {
        return this.currentUserId;
    }
    public void setCurrentUserId(long currentUserId) {
        this.currentUserId = currentUserId;
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1423205226)
    public User getCurrentUser() {
        long __key = this.currentUserId;
        if (currentUser__resolvedKey == null || !currentUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            User currentUserNew = targetDao.load(__key);
            synchronized (this) {
                currentUser = currentUserNew;
                currentUser__resolvedKey = __key;
            }
        }
        return currentUser;
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
    @Generated(hash = 2094502880)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAuthCredentialDao() : null;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1113642023)
    public void setCurrentUser(@NotNull User currentUser) {
        if (currentUser == null) {
            throw new DaoException(
                    "To-one property 'currentUserId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.currentUser = currentUser;
            currentUserId = currentUser.getId();
            currentUser__resolvedKey = currentUserId;
        }
    }
}
