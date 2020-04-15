package sdk.chat.core.session;

import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.MessageDao;
import sdk.chat.core.dao.ReadReceiptUserLink;
import sdk.chat.core.dao.ReadReceiptUserLinkDao;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.ThreadDao;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.dao.UserThreadLinkDao;
import sdk.chat.core.interfaces.CoreEntity;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.core.utils.TimeLog;

import static sdk.chat.core.dao.DaoCore.daoSession;
import static sdk.chat.core.dao.DaoCore.fetchEntityWithProperty;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class StorageManager {

    public List<Thread> fetchThreadsForCurrentUser() {
        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());

        List<Thread> threads = new ArrayList<>();

        List<UserThreadLink> links = DaoCore.fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.UserId, ChatSDK.currentUser().getId());

        for (UserThreadLink link : links) {
            Thread thread = link.getThread();
            if (thread != null) {
                threads.add(thread);
            }
            else {
                // Delete the link - it's obviously corrupted
                link.delete();
            }
        }

        TimeLog.endTimeLog();
        return threads;
    }

    public ReadReceiptUserLink readReceipt(Long messageId, Long userId) {
        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());

        QueryBuilder<ReadReceiptUserLink> queryBuilder = daoSession.queryBuilder(ReadReceiptUserLink.class);
        queryBuilder.where(ReadReceiptUserLinkDao.Properties.UserId.eq(userId)).where(ReadReceiptUserLinkDao.Properties.MessageId.eq(messageId));
        List<ReadReceiptUserLink> links = queryBuilder.list();

        TimeLog.endTimeLog();

        if (!links.isEmpty()) {
            return links.get(0);
        }

        return null;
    }

    public <T extends CoreEntity> T fetchOrCreateEntityWithEntityID(Class<T> c, String entityId){
        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());

        T entity = DaoCore.fetchEntityWithEntityID(c, entityId);
//
        if (entity == null) {
            entity = DaoCore.getEntityForClass(c);
            entity.setEntityID(entityId);
            entity = DaoCore.createEntity(entity);
        }

        TimeLog.endTimeLog();

        return entity;
    }

    public <T> T createEntity (Class<T> c) {
        T entity = DaoCore.getEntityForClass(c);
        DaoCore.createEntity(entity);
        return entity;
    }

    public <T extends CoreEntity> T insertOrReplaceEntity (T entity) {
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

    public List<Message> fetchUnreadMessagesForThread (Long threadId) {
        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());

        Long currentUserId = ChatSDK.currentUser().getId();

        QueryBuilder<Message> qb = daoSession.queryBuilder(Message.class);
        Join<Message, ReadReceiptUserLink> join = qb.where(qb.and(MessageDao.Properties.ThreadId.eq(threadId), MessageDao.Properties.SenderId.notEq(currentUserId)))
                .join(ReadReceiptUserLink.class, ReadReceiptUserLinkDao.Properties.MessageId);

        join.where(join.and(ReadReceiptUserLinkDao.Properties.UserId.eq(currentUserId), ReadReceiptUserLinkDao.Properties.Status.notEq(ReadStatus.Read)));

        List<Message> list = qb.list();

        TimeLog.endTimeLog();

        return list;
    }

//    public int fetchUnreadMessageCount(ThreadType threadType) {
//        Long currentUserId = ChatSDK.currentUser().getId();
//
//        QueryBuilder<ReadReceiptUserLink> qb = daoSession.queryBuilder(ReadReceiptUserLink.class);
//        Join<?, Message> message = qb.join(Message.class, ReadReceiptUserLinkDao.Properties.MessageId);
//
//        Join<Message, ReadReceiptUserLink> rrj;
//
//        Join<ReadReceiptUserLink, Thread> thread = qb.join(rrj, null, Thread.class, null);
//
//
//        QueryBuilder<Thread> qb2 = daoSession.queryBuilder(Thread.class);
//        Join<Thread, Message> join = qb2.join(Message.class, MessageDao.Properties.ThreadId);
//        Join<Message, ReadReceiptUserLink> join2 = qb2.join(join, MessageDao.Properties.Id, ReadReceiptUserLink.class, ReadReceiptUserLinkDao.Properties.MessageId);
//
//        Join message = qb.join(Message.class, MessageDao.Properties.ThreadId).where(MessageDao.Properties.SenderId.notEq(currentUserId));
//
//        Join<Message, ReadReceiptUserLink> readReceipt = qb.join(rrj, MessageDao.Properties., ReadReceiptUserLink.class, ReadReceiptUserLinkDao.Properties.MessageId);
//
//        join.where(join.and(ReadReceiptUserLinkDao.Properties.UserId.eq(currentUserId), ReadReceiptUserLinkDao.Properties.Status.notEq(ReadStatus.Read)));
//        return qb.list().size();
//    }

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
        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());

        List<UserThreadLink> links =  DaoCore.fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.UserId, ChatSDK.currentUser().getId());
        ArrayList<Thread> threads = new ArrayList<>();
        for(UserThreadLink link : links) {
            threads.add(link.getThread());
        }

        TimeLog.endTimeLog();

        return threads;
    }

//    public List<Message> fetchMessagesForThreadWithID (long threadID, int limit) {
//        return fetchMessagesForThreadWithID(threadID, limit, null);
//    }

    public List<Message> fetchMessagesForThreadWithID (long threadID, int limit, Date olderThan) {
        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());

        // If we have a zero date, treat it as null
        if (olderThan != null && olderThan.equals(new Date(0))) {
            olderThan = null;
        }

        QueryBuilder<Message> qb = daoSession.queryBuilder(Message.class);
        qb.where(MessageDao.Properties.ThreadId.eq(threadID));

        // Making sure no null messages infected the sort.
        qb.where(MessageDao.Properties.Date.isNotNull());
        qb.where(MessageDao.Properties.SenderId.isNotNull());

        if(olderThan != null) {
            qb.where(MessageDao.Properties.Date.lt(olderThan.getTime()));
        }

        qb.orderDesc(MessageDao.Properties.Date);

        if (limit != -1) {
            qb.limit(limit);
        }

        List<Message> list = qb.list();

        TimeLog.endTimeLog();

        return  list;
    }

    public void update(CoreEntity entity) {
        DaoCore.updateEntity(entity);
    }

    public void delete(Object entity) {
        DaoCore.deleteEntity(entity);
    }

}
