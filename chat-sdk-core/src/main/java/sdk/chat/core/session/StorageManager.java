package sdk.chat.core.session;

import android.content.Context;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.query.QueryBuilder;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.CachedFileDao;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.MessageDao;
import sdk.chat.core.dao.PublicKey;
import sdk.chat.core.dao.PublicKeyDao;
import sdk.chat.core.dao.ReadReceiptUserLink;
import sdk.chat.core.dao.ReadReceiptUserLinkDao;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.ThreadDao;
import sdk.chat.core.dao.Updatable;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserDao;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.dao.UserThreadLinkDao;
import sdk.chat.core.interfaces.CoreEntity;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class StorageManager {

    protected Map<String, CoreEntity> entityCache = new HashMap<>();
    protected boolean entityCacheEnabled = true;

    protected DaoCore daoCore;

    public StorageManager(Context context) {
        daoCore = new DaoCore(context);
    }

    public DaoCore getDaoCore() {
        return daoCore;
    }

    public List<Thread> fetchThreadsForCurrentUser() {
//        Logger.debug(java.lang.Thread.currentThread().getName());

        List<Thread> threads = new ArrayList<>();

        List<UserThreadLink> links = daoCore.fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.UserId, ChatSDK.currentUser().getId());

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

        return threads;
    }

    public Single<List<Thread>> fetchThreadsForCurrentUserAsync() {
        return Single.defer(() -> Single.just(fetchThreadsForCurrentUser())).subscribeOn(RX.db());
    }

    public ReadReceiptUserLink readReceipt(Long messageId, Long userId) {
        // Logger.debug(java.lang.Thread.currentThread().getName());

        QueryBuilder<ReadReceiptUserLink> queryBuilder = daoCore.getDaoSession().queryBuilder(ReadReceiptUserLink.class);
        queryBuilder.where(ReadReceiptUserLinkDao.Properties.UserId.eq(userId)).where(ReadReceiptUserLinkDao.Properties.MessageId.eq(messageId));
        List<ReadReceiptUserLink> links = queryBuilder.list();

        if (links.size() > 1) {
            Logger.debug("Multiple read receipts for one user");
            for (int i = 1; i < links.size(); i++) {
                links.get(i).delete();
            }
        }

        if (!links.isEmpty()) {
            return links.get(0);
        }

        return null;
    }

    public Single<Optional<ReadReceiptUserLink>> readReceiptAsync(Long messageId, Long userId) {
        return Single.defer(() -> Single.just(new Optional<>(readReceipt(messageId, userId)))).subscribeOn(RX.db());
    }

    public Message fetchOrCreateMessageWithEntityID(String entityId) {
        return fetchOrCreateEntityWithEntityID(Message.class, entityId);
    }

    public synchronized <T extends CoreEntity> T fetchOrCreateEntityWithEntityID(Class<T> c, String entityId) {

        T entity = fetchEntityWithEntityID(entityId, c);

        if (entity == null) {
            entity = createEntity(c, entityId);
//            entity.setEntityID(entityId);
//            entity.update();

        }

        return entity;
    }

    public synchronized CachedFile fetchCachedFileWithHash(String hash, String messageId) {
        QueryBuilder<CachedFile> qb = daoCore.getDaoSession().queryBuilder(CachedFile.class);
        qb.where(CachedFileDao.Properties.Hash.eq(hash));
        qb.where(CachedFileDao.Properties.Identifier.eq(messageId));

        try {
            return qb.unique();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized CachedFile fetchOrCreateCachedFileWithHash(String hash, String messageId) {
        CachedFile cachedFile = fetchCachedFileWithHash(hash, messageId);
        if (cachedFile == null) {
            cachedFile = new CachedFile();
            cachedFile.setEntityID(UUID.randomUUID().toString());
            cachedFile.setHash(hash);
            cachedFile.setIdentifier(messageId);
            daoCore.createEntity(cachedFile);
        }
        return cachedFile;

    }

    public <T extends CoreEntity> Single<T> fetchOrCreateEntityWithEntityIDAsync(Class<T> c, String entityId) {
        return Single.defer(() -> Single.just(fetchOrCreateEntityWithEntityID(c, entityId))).subscribeOn(RX.db());
    }

//    public synchronized <T> T create(Class<T> c) {
//        T entity = daoCore.getEntityForClass(c);
//        daoCore.createEntity(entity);
//        return entity;
//    }
    public synchronized <T extends CoreEntity> T createEntity(Class<T> c) {
        return createEntity(c, null);
    }

    public synchronized <T extends CoreEntity> T createEntity(Class<T> c, String entityID) {
        T entity = null;

        if (c == User.class) {
            entity = (T) new User();
        }
        if (c == Thread.class) {
            entity = (T) new Thread();
        }
        if (c == Message.class) {
            entity = (T) new Message();
        }
        if (c == PublicKey.class) {
            entity = (T) new PublicKey();
        }
        if (c == CachedFile.class) {
            entity = (T) new CachedFile();
        }
        if (entityID != null) {
            entity.setEntityID(entityID);
        }

        if(entity != null) {
            daoCore.createEntity(entity);
        }

        return entity;
    }

    public <T extends CoreEntity> T insertOrReplaceEntity(T entity) {
        daoCore.createEntity(entity);
        return entity;
    }

    public <T extends CoreEntity> Single<T> insertOrReplaceEntityAsync(T entity) {
        return Single.defer(() -> Single.just(insertOrReplaceEntity(entity)).subscribeOn(RX.db()));
    }

    public synchronized <T extends CoreEntity> T fetchEntityWithEntityID(String entityID, Class<T> c) {
        if (entityID == null) {
            return null;
        }

        T entity = null;

        if (entityCacheEnabled) {
            entity = (T) entityCache.get(c + entityID);
        }

        if (entity != null) {
            return entity;
        }

        QueryBuilder<T> qb = daoCore.getDaoSession().queryBuilder(c);

        if (c == Thread.class) {
            qb.where(ThreadDao.Properties.EntityID.eq(entityID));
        }
        else if (c == PublicKey.class) {
            qb.where(PublicKeyDao.Properties.EntityID.eq(entityID));
        }
        else if (c == Message.class) {
            qb.where(MessageDao.Properties.EntityID.eq(entityID));
        }
        else if (c == User.class) {
            qb.where(UserDao.Properties.EntityID.eq(entityID));
        }
        else if (c == CachedFile.class) {
            qb.where(CachedFileDao.Properties.EntityID.eq(entityID));
        }

        try {
//            List<T> entities = qb.list();
//            if (entities.isEmpty()) {
//                return null;
//            } else {
//                entity = entities.get(0);
//            }
            entity = qb.unique();
        } catch (Exception e) {
            Logger.warn("Message doesn't exist");
            return null;
        }

        if (entity != null && entityCacheEnabled) {
            entityCache.put(c + entityID, entity);
        }

        return entity;
    }

    public <T extends CoreEntity> Single<T> fetchEntityWithEntityIDAsync(String entityID, Class<T> c) {
        return Single.defer(() -> Single.just(fetchEntityWithEntityID(entityID, c)).subscribeOn(RX.db()));
    }

    public Single<User> fetchUserWithEntityIDAsync(String entityID) {
        return Single.defer(() -> Single.just(fetchUserWithEntityID(entityID)).subscribeOn(RX.db()));
    }

    public List<Thread> fetchThreadsWithType(int type) {
        QueryBuilder<Thread> qb = daoCore.getDaoSession().queryBuilder(Thread.class);
        qb.where(ThreadDao.Properties.Type.eq(type));
        return qb.list();
    }

    public synchronized Thread fetchOrCreateThreadWithEntityID(String entityId) {
        return fetchOrCreateEntityWithEntityID(Thread.class, entityId);
    }

    public synchronized User fetchUserWithEntityID(String entityID) {
        return fetchEntityWithEntityID(entityID, User.class);
    }

    public synchronized Thread fetchThreadWithEntityID(String entityID) {
        return fetchEntityWithEntityID(entityID, Thread.class);
    }

    public synchronized Message fetchMessageWithEntityID(String entityID) {
        return fetchEntityWithEntityID(entityID, Message.class);
    }

    public Single<List<Thread>> fetchThreadsWithTypeAsync(int type) {
        return Single.defer(() -> Single.just(fetchThreadsWithType(type)).subscribeOn(RX.db()));
    }

    public List<Message> fetchUnreadMessagesForThread(Long threadId) {
        // Logger.debug(java.lang.Thread.currentThread().getName());

        Long currentUserId = ChatSDK.currentUser().getId();

        QueryBuilder<Message> qb = daoCore.getDaoSession().queryBuilder(Message.class);
        qb.where(MessageDao.Properties.ThreadId.eq(threadId), MessageDao.Properties.SenderId.notEq(currentUserId), MessageDao.Properties.IsRead.eq(false));
        return qb.list();

//        Join<Message, ReadReceiptUserLink> join = qb.where(qb.and(MessageDao.Properties.ThreadId.eq(threadId), MessageDao.Properties.SenderId.notEq(currentUserId)))
//                .join(ReadReceiptUserLink.class, ReadReceiptUserLinkDao.Properties.MessageId);
//
//        join.where(join.and(ReadReceiptUserLinkDao.Properties.UserId.eq(currentUserId), ReadReceiptUserLinkDao.Properties.Status.notEq(ReadStatus.Read)));
//
//        List<Message> list = qb.list();

//        return list;
    }

    public Single<List<Message>> fetchUnreadMessagesForThreadAsync(Long threadId) {
        return Single.defer(() -> Single.just(fetchUnreadMessagesForThread(threadId)).subscribeOn(RX.db()));
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

    public Thread fetchThreadWithID(long threadID) {
        return daoCore.fetchEntityWithProperty(Thread.class, ThreadDao.Properties.Id, threadID);
    }

    public Single<Thread> fetchThreadWithIDAsync (long threadID) {
        return Single.defer(() -> Single.just(fetchThreadWithID(threadID)).subscribeOn(RX.db()));
    }

    public Single<Thread> fetchThreadWithEntityIDAsync (String entityID) {
        return Single.defer(() -> Single.just(fetchThreadWithEntityID(entityID)).subscribeOn(RX.db()));
    }

    public Thread fetchThreadWithUsers (List<User> users) {
        // Logger.debug(java.lang.Thread.currentThread().getName());
        Set<User> set = new HashSet<>(users);
        for(Thread t : allThreads()) {
            Set<User> compareTo = new HashSet<>(t.getUsers());
            if(set.equals(compareTo)) {
                return t;
            }
        }
        return null;
    }

//    public Single<Thread> fetchThreadWithUsersAsync (List<User> users) {
//        return Single.defer(() -> Single.just(fetchThreadWithUsers(users)).subscribeOn(RX.db()));
//    }

    public List<Thread> allThreads() {
        // Logger.debug(java.lang.Thread.currentThread().getName());

        List<UserThreadLink> links =  ChatSDK.db().getDaoCore().fetchEntitiesWithProperty(UserThreadLink.class, UserThreadLinkDao.Properties.UserId, ChatSDK.currentUser().getId());
        ArrayList<Thread> threads = new ArrayList<>();
        for(UserThreadLink link : links) {
            threads.add(link.getThread());
        }

        return threads;
    }

    public Single<List<Thread>> allThreadsAsync() {
        return Single.defer(() -> Single.just(allThreads()).subscribeOn(RX.db()));
    }


    public List<Message> fetchMessagesForThreadWithID (long threadID, @Nullable Date from, @Nullable Date to, int limit) {
        // Logger.debug(java.lang.Thread.currentThread().getName());

        // If we have a zero date, treat it as null
        if (to != null && to.equals(new Date(0))) {
            to = null;
        }
        if (from != null && from.equals(new Date(0))) {
            from = null;
        }

        QueryBuilder<Message> qb = daoCore.getDaoSession().queryBuilder(Message.class);
        qb.where(MessageDao.Properties.ThreadId.eq(threadID));

        // Making sure no null messages infected the sort.
        qb.where(MessageDao.Properties.Date.isNotNull());
        qb.where(MessageDao.Properties.SenderId.isNotNull());

        if(to != null) {
            qb.where(MessageDao.Properties.Date.lt(to.getTime()));
        }
        if(from != null) {
            qb.where(MessageDao.Properties.Date.gt(from.getTime()));
        }

        qb.orderDesc(MessageDao.Properties.Date);

        if (limit > 0) {
            qb.limit(limit);
        }

        return qb.list();
    }

    public List<CachedFile> fetchFilesWithIdentifier(String identifier) {
        QueryBuilder<CachedFile> qb = daoCore.getDaoSession().queryBuilder(CachedFile.class);
        qb.where(CachedFileDao.Properties.Identifier.eq(identifier));
        return qb.list();
    }

    public List<Message> fetchMessagesWithFailedDecryption() {
        // Logger.debug(java.lang.Thread.currentThread().getName());

//        QueryBuilder<Thread> qb = daoSession.queryBuilder(Thread.class);
//        qb.where(ThreadDao.Properties.UserAccountID.eq(ChatSDK.currentUserID()))
//                .join(Message.class, MessageDao.Properties.ThreadId).where(MessageDao.Properties.DecryptionFailed.eq(true));


//        return qb.list();

        QueryBuilder<Message> qb = daoCore.getDaoSession().queryBuilder(Message.class);
        qb.where(MessageDao.Properties.EncryptedText.isNotNull())
                .join(MessageDao.Properties.ThreadId, Thread.class);
        return qb.list();

//        qb.where(MessageDao.Properties.ThreadId.eq(threadID));
//        qb.where(MessageDao.Properties.DecryptionFailed.eq(true));
//
//        QueryBuilder<Message> qb = daoSession.queryBuilder(Message.class);
//
//        Join<Message, Thread> join = qb.where(qb.and(MessageDao.Properties.ThreadId.eq(threadId), MessageDao.Properties.SenderId.notEq(currentUserId)))
//                .join(ReadReceiptUserLink.class, ReadReceiptUserLinkDao.Properties.MessageId);
//
//        return qb.list();
    }

    public PublicKey getPublicKey(String userId) {
        return fetchEntityWithEntityID(userId, PublicKey.class);
    }

    public void deletePublicKey(String userId) {
        PublicKey key = getPublicKey(userId);
        if (key != null) {
            daoCore.deleteEntity(key);
        }
    }

    public void deleteAllPublicKeys() {
        List<PublicKey> keys = daoCore.fetchEntitiesOfClass(PublicKey.class);
        for (PublicKey key: keys) {
            daoCore.deleteEntity(key);
        }
    }

    public void addPublicKey(String userId, String identifier, String key) {
        PublicKey publicKey = fetchOrCreateEntityWithEntityID(PublicKey.class, userId);
        publicKey.setKey(key);
        publicKey.setIdentifier(identifier);
        daoCore.updateEntity(publicKey);
    }

    public Single<List<Message>> fetchMessagesForThreadWithIDAsync(long threadID, Date from, Date to, int limit) {
        return Single.defer(() -> Single.just(fetchMessagesForThreadWithID(threadID, from, to, limit)).subscribeOn(RX.db()));
    }

    public void update(CoreEntity entity) {
        daoCore.updateEntity(entity);
    }

    public Completable updateAsync(CoreEntity entity) {
        return Completable.defer(() -> {
            update(entity);
            return Completable.complete();
        }).subscribeOn(RX.db());
    }

    public void delete(Object entity) {
        daoCore.deleteEntity(entity);
    }

    public Completable deleteAsync(Object entity) {
        return Completable.defer(() -> {
            delete(entity);
            return Completable.complete();
        }).subscribeOn(RX.db());
    }

    public void openDatabase(String name) throws Exception {
        daoCore.openDB(name);
    }

    public void closeDatabase() {
        daoCore.closeDB();
        entityCache.clear();
    }

    public boolean isDatabaseOpen() {
        return daoCore != null && daoCore.getDaoSession() != null;
    }

    public void update(Updatable updatable) {
        update(updatable, null);
    }

    public void update(Updatable updatable, Runnable then) {
        update(updatable, then, true);
    }

    public void update(Updatable updatable, boolean async) {
        update(updatable, null, true);
    }

    public void update(Updatable updatable, Runnable then, boolean async) {
        if (entityCacheEnabled && async) {
            RX.single().scheduleDirect(() -> {
                updatable.update();
                if (then != null) {
                    then.run();
                }
            });
        } else {
            updatable.update();
            if (then != null) {
                then.run();
            }
        }
    }
}


