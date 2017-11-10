package co.chatsdk.core.session;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.MessageDao;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.ThreadDao;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.UserThreadLink;
import co.chatsdk.core.dao.UserThreadLinkDao;
import co.chatsdk.core.interfaces.CoreEntity;
import co.chatsdk.core.interfaces.StorageAdapter;
import timber.log.Timber;

import static co.chatsdk.core.dao.DaoCore.daoSession;
import static co.chatsdk.core.dao.DaoCore.fetchEntityWithProperty;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class StorageManager {

    private final static StorageManager instance = new StorageManager();
    public StorageAdapter a;

    public static StorageManager shared() {
        return instance;
    }

    public <T extends CoreEntity> T fetchOrCreateEntityWithEntityID(Class<T> c, String entityId){

        T entity = DaoCore.fetchEntityWithEntityID(c, entityId);
//
        if (entity == null)
        {
            entity = DaoCore.getEntityForClass(c);

            if(entityId instanceof String) {
                entity.setEntityID((String) entityId);
            }
            else {
                entity.setEntityID(entityId.toString());
                Timber.v("ERROR!!! The entity must always be a string");
            }

            entity = DaoCore.createEntity(entity);
        }

        return entity;
    }

    public <T extends CoreEntity> T createEntity (Class<T> c) {
        T entity = DaoCore.getEntityForClass(c);
        DaoCore.createEntity(entity);
        return entity;
    }

    public <T extends CoreEntity> T fetchEntityWithEntityID(Object entityID, Class<T> c) {
        return DaoCore.fetchEntityWithEntityID(c, entityID);
    }


    public User fetchUserWithEntityID (String entityID) {
        return DaoCore.fetchEntityWithEntityID(User.class, entityID);
    }

    public List<Thread> fetchThreadsWithType (int type) {
        return DaoCore.fetchEntitiesWithProperty(Thread.class, ThreadDao.Properties.Type, type);
    }

    public Thread fetchThreadWithID (long threadID) {
        return fetchEntityWithProperty(Thread.class, ThreadDao.Properties.Id, threadID);
    }

    public Thread fetchThreadWithEntityID (String entityID) {
        if(entityID != null) {
            return fetchEntityWithProperty(Thread.class, ThreadDao.Properties.EntityID, entityID);
        }
        return null;
    }

    public Thread fetchThreadWithUsers (List<User> users) {
        for(Thread t : allThreads()) {
            if(t.getUsers().equals(users)) {
                return t;
            }
        }
        return null;
    }

    public List<Thread> allThreads () {
        List<UserThreadLink> links =  DaoCore.fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.UserId, NM.currentUser().getId());
        ArrayList<Thread> threads = new ArrayList<>();
        for(UserThreadLink link : links) {
            threads.add(link.getThread());
        }
        return threads;
    }

    public List<Message> fetchMessagesForThreadWithID (long threadID, int limit) {
        return fetchMessagesForThreadWithID(threadID, limit, null);
    }

    public List<Message> fetchMessagesForThreadWithID (long threadID, int limit, Date olderThan) {
        List<Message> list ;

        QueryBuilder<Message> qb = daoSession.queryBuilder(Message.class);
        qb.where(MessageDao.Properties.ThreadId.eq(threadID));

        // Making sure no null messages infected the sort.
        qb.where(MessageDao.Properties.Date.isNotNull());
        qb.where(MessageDao.Properties.SenderId.isNotNull());

        if(olderThan != null) {
            qb.where(MessageDao.Properties.Date.lt(olderThan.getTime()));
        }

        qb.orderDesc(MessageDao.Properties.Date);

        if (limit != -1)
            qb.limit(limit);

        list = qb.list();

        return list;

    }

}
