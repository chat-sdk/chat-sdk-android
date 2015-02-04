package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.sorter.MessageSorter;
import com.braunster.chatsdk.dao.BFollower;
import com.braunster.chatsdk.dao.BLinkData;
import com.braunster.chatsdk.dao.BLinkDataDao;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.BPath;
import com.braunster.chatsdk.object.BError;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;


/* *
Created by itzik on 6/8/2014.
*/


public class BFirebaseInterface {

    private static final String TAG = BFirebaseInterface.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseInterface;

    private static BFirebaseInterface instance;

    public static BFirebaseInterface getInstance() {
        if (instance == null)
            instance = new BFirebaseInterface();

        return instance;
    }

    private BFirebaseInterface(){

    }

    /** Push an entity to the firebase server, If the entities has children they will be pushed to.
     *  If you want to push an entity that has children you shouldn't provide the class type to the
     *  {@link com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError }
     *  so it will return and object on it
     *  {@link com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError#onItem onItem} callback method.*/
    public static <T extends Entity>  void pushEntity(final T entity, final RepetitiveCompletionListenerWithError<T, BError> listener){
        // We're going to be pushing an entity which may have sub entities
        // we need to call status for each item that completes then call
        // completion when they all complete
        Collection<Entity> entitiesToPush = new ArrayList<Entity>();

        // Adding the parent entity after so it will be the first to be added.
        entitiesToPush.add(entity);

        //Note push children only if the parent has entity id. If he dont have entity the child will be added in the wrong place.
        if (entity.getEntityID() != null)
            // Adding child entities if has any.
            if (entity.getChildren() != null && entity.getChildren().size() > 0)
            {
                if (DEBUG) Log.d(TAG, "Entity has children. Amount: " + entity.getChildren().size());
                entitiesToPush.addAll( entity.getChildren());
                if (DEBUG) Log.d(TAG, "entitiesToPush: " + entitiesToPush.size());
            }

        PushCompletedListener<T> comListener = new PushCompletedListener<T>(listener, entitiesToPush.size());

        for (Entity e : entitiesToPush){
            if (DEBUG){
                Log.v(TAG, "pushEntity loop");
                DaoCore.printEntity(e);
            }

            implPushEntity((T) e, comListener);

            // The onItem method is returning a boolean value that indicated if the caller want to continue pushing entities.
             if(comListener.isStoped())
                 break;
        }
    }

    /** For each item that is finished his push a method in this class will called. if the caller method returns true the pushing will be stopped.*/
    public static class PushCompletedListener <T extends Entity> implements CompletionListenerWithDataAndError<T, BError>{
        /** Keep the value returned from <b>onItem</b>, If value is false we stop the pushing.
         * Keep in mind that the actual stop is made by the loop on the <b>pushEntity</b> method so unless it's a huge amount this wont prevent the push..*/
        public boolean stop = false;
        /** The amount of entities that needs to be pushed, We need this so we know when all the pushes is done and we can trigger <b>onDone</b>.*/
        private int pushesAmount = 0;
        private RepetitiveCompletionListenerWithError<T, BError> listener;

        public PushCompletedListener(RepetitiveCompletionListenerWithError<T, BError> listener, int pushesAmount){
            this.listener = listener;
            this.pushesAmount = pushesAmount;
        }

        @Override
        public void onDone(T entity) {
            pushesAmount--;

            /*This will only affect entities that has this parameter, For example BUser, BThread, BMessage, BMetadata*/
            entity.setAsDirty(false);
            DaoCore.updateEntity(entity);

            if (listener != null)
                stop = listener.onItem(entity);

            if (listener != null)
                if (pushesAmount <= 0)
                    listener.onDone();
        }

        @Override
        public void onDoneWithError(T entity, BError error) {
            if (listener != null)
                listener.onItemError(entity, error);
        }

        public boolean isStoped() {
            return stop;
        }
    }

    public static <T extends Entity>  void implPushEntity(final T entity, final CompletionListenerWithDataAndError<T, BError> listener){
        if (entity.isDirty()){

            Object priority = entity.getPriority();

            if (StringUtils.isEmpty(entity.getEntityID()) && priority != null){
                selectEntity(entity, new CompletionListenerWithDataAndError<T, BError>() {
                    @Override
                    public void onDone(T en) {
                        try {
                            // Call the method to push the entity,
                            // If entity selected is null use the entity given in the "impl_pushEntity" call.
                            new pushEntity( (en==null?entity:en) , listener).call();
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onDoneWithError(entity, BError.getExceptionError(e));
                        }
                    }

                    @Override
                    public void onDoneWithError(T en, BError error) {
                        listener.onDoneWithError(en, error);
                    }
                });
            }
            else  try {
                // Call the method to push the entity,
                // If entity selected is null use the entity given in the "impl_pushEntity" call.
                new pushEntity( entity , listener).call();
            } catch (Exception e) {
                e.printStackTrace();
                listener.onDoneWithError(entity, BError.getExceptionError(e));
            }
        }
        // Let the listener know we pushed a new entity.
        else {
            if (DEBUG) Log.d(TAG, "Entity is not dirty");
            listener.onDone(entity);
        }
    }

    private static class pushEntity<T extends Entity> implements Callable{

        private T entity;
        private CompletionListenerWithDataAndError<T, BError> listener;

        public pushEntity(T entity, CompletionListenerWithDataAndError<T, BError> listener){
            this.entity = entity;
            this.listener = listener;
        }

        @Override
        public Void call() throws Exception {
            if (DEBUG) Log.d(TAG, "PushEntityClass, entity: " + (entity.getBPath() != null ? entity.getBPath().getPath(): entity.getEntityID()) + (entity.getClass().equals(BUser.class) ? ((BUser) entity).getMetaName() : "" ));
            if (entity.getBPath() == null)
            {
                if (DEBUG) Log.e(TAG, "Cant get path from entity");
                listener.onDoneWithError(entity, BError.getNoPathError());
                return null;
            }

            FirebasePaths ref = FirebasePaths.firebaseRef();
            if (DEBUG) Log.d(TAG, "pushEntityClass, RefPath: " + ref.toString());
            ref = ref.appendPathComponent(entity.getBPath().getPath());
            if (DEBUG) Log.d(TAG, "pushEntityClass, RefPath: " + ref.toString());

            // If the entity has id that means its already added to the database and only need to be updated.
            if (StringUtils.isNotEmpty(entity.getEntityID()) && StringUtils.isNotBlank(entity.getEntityID()))
            {
                ref.updateChildren(entity.asMap(), new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError == null)
                            listener.onDone(entity);
                        else
                        {
                            if (DEBUG) Log.e(TAG, "Error while updating entity children");
                            listener.onDoneWithError(entity, BFirebaseNetworkAdapter.getFirebaseError(firebaseError));
                        }
                    }
                });
            }
            else
            {
                if (DEBUG) Log.d(TAG, "Selected Entity is null, Creating Entity...Entity priority: " + entity.getPriority());
                // Pushing a new child to the list.
                Firebase listRef = ref.push();
                // Set the entity id to the new child added name.
                entity.setEntityID(listRef.getKey());

                // Saving the entity
                DaoCore.updateEntity(entity);

                /*// If the values from the entity have a specific path where they need to be placed.
                if (!entity.mapPath().equals(""))
                    listRef = listRef.child(entity.mapPath());
*/
                listRef.setValue(entity.asMap(), entity.getPriority(), new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError == null)
                        {
                            if (DEBUG) Log.d(TAG, "Entity is pushed.");
                            listener.onDone(entity);
                        }
                        else listener.onDoneWithError(entity, BFirebaseNetworkAdapter.getFirebaseError(firebaseError));
                    }
                });
            }

            return null;
        }
    }

    /** Get entity from the firebase server.*/
    public static <T extends Entity> void selectEntity(final T entity, final CompletionListenerWithDataAndError<T, BError> listener){

        if (entity.getBPath() == null)
        {
            if (DEBUG) Log.e(TAG, "Cant push entity with no path");
            listener.onDoneWithError(entity, BError.getNoPathError());
        }

        FirebasePaths ref = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());
        ref = ref.appendPathComponent(entity.getBPath().getPath());
        if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());

        if (entity.getEntityID() != null && entity.getEntityID().length() > 0)
        {
            if (DEBUG) Log.d(TAG, "Entity has id");
            // Get the object by its address, If exists.
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (DEBUG) Log.v(TAG, "onDataChanged");
                    Object obj = objectFromSnapshot(dataSnapshot);

                    if (obj  instanceof Object[])
                        obj = ((Object[])obj)[0];

                    listener.onDone(obj!=null? (T) obj :entity);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (DEBUG) Log.e(TAG, "onCancelled");
                    listener.onDoneWithError(entity, BFirebaseNetworkAdapter.getFirebaseError(firebaseError));
                }
            });
            
        }
        // If no address exists get the object by its priority.
        else
        {
            // Check if has priority
            if (entity.getPriority() != null && !entity.getPriority().equals(""))
            {
                if (DEBUG) Log.d(TAG, "Getting entity by priority");

                Query query = ref.startAt(String.valueOf(
                        entity.getPriority())).endAt(String.valueOf(entity.getPriority()));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (DEBUG) Log.v(TAG, "onDataChanged");
                        if (dataSnapshot.hasChildren())
                        {
                            if(DEBUG) Log.d(TAG, "HasChildren");
                            Object[] objects = childrenFromSnapshot(dataSnapshot);
                            Object obj = objects[0];
                            listener.onDone(objects.length > 0 ? (T) (obj != null ? obj : entity) : entity);
                        }
                        else
                        {
                            if(DEBUG) Log.d(TAG, "No Children");
                            Object obj = objectFromSnapshot(dataSnapshot);
                            listener.onDone(obj!=null? (T) obj :entity);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        if (DEBUG) Log.e(TAG, "onCancelled");
                        listener.onDoneWithError(entity, BFirebaseNetworkAdapter.getFirebaseError(firebaseError));
                    }
                });
            }
            else
            {
                if (DEBUG) Log.d(TAG, "No priority.");
                listener.onDone(entity);
            }
        }
    }

    public static void loadMoreMessagesForThread(BThread thread, BMessage earliestMessage, int numOfMessages, final CompletionListenerWithData<BMessage[]> listener){
        final Date messageDate;

        // If we have a message in the database then we use the earliest
        if (earliestMessage != null)
        {
            if(DEBUG) Log.d(TAG, "Msg: " + earliestMessage.getText());
            messageDate = earliestMessage.getDate();
        }
        // Otherwise we use todays date
        else messageDate = new Date();


        List<BMessage> list ;

        QueryBuilder<BMessage> qb = DaoCore.daoSession.queryBuilder(BMessage.class);
        qb.where(BMessageDao.Properties.OwnerThread.eq(thread.getId()));

        // Making sure no null messages infected the sort.
        qb.where(BMessageDao.Properties.Date.isNotNull());

        qb.where(BMessageDao.Properties.Date.lt(messageDate));

        qb.limit(numOfMessages + 1);
        qb.orderDesc(BMessageDao.Properties.Date);

        list = qb.list();

        // If we have older messages in the db we get them
        if (list.size() > 0){
            if (DEBUG) Log.d(TAG, "Loading messages from local db, " + list.size());
            Collections.sort(list, new MessageSorter(DaoCore.ORDER_DESC));

            if(DEBUG) Log.d(TAG, "Msg: " + list.get(0).getText());

            if (listener!=null)
                listener.onDone(list.toArray(new BMessage[list.size()]));
        }
        else
        {
            if (DEBUG) Log.d(TAG, "Loading messages from firebase");

            Firebase messageRef = FirebasePaths.threadRef(thread.getEntityID()).appendPathComponent(BFirebaseDefines.Path.BMessagesPath);

            // Get # messages ending at the end date
            // Limit to # defined in BFirebaseDefines
            // We add one becase we'll also be returning the last message again
            Query msgQuery = messageRef.endAt(messageDate.getTime()).limitToLast(numOfMessages + 1);

            msgQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Object[] objs = (Object[]) objectFromSnapshot(snapshot);

                    BMessage[] messages;
                    if (objs!= null && objs.length > 0)
                    {
                        messages = new BMessage[objs.length];
                        for (int i = 0 ; i < objs.length; i++)
                            if (objs[i] instanceof BMessage)
                                messages[i] = (BMessage) objs[i];
                    }
                    else messages = new BMessage[0];

                    if (listener != null)
                        listener.onDone(messages);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (listener != null)
                        listener.onDone( new BMessage[0]);
                }
            });
        }
    }

    public static Object objectFromSnapshot(DataSnapshot dataSnapshot){
        if (dataSnapshot == null)
        {
            if (DEBUG) Log.e(TAG, "objectFromSnapshot, Snapshot is null.");
            return null;
        }
        else if (dataSnapshot.getValue() == null)
        {
            if (DEBUG) Log.e(TAG, "objectFromSnapshot, Values is null.");
            return null;
        }

        if (DEBUG)Log.v(TAG, "objectFromSnapshot, Path: " + dataSnapshot.getRef().getPath().toString());
        BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());

        // ---------------
        // User Class Type And thread from user
        if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUser");
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(userFirebaseID))
            {
                if (DEBUG) Log.i(TAG, "objectFromSnapshot, Returning BUser");
                return getUser(dataSnapshot, userFirebaseID);
            }
            else {
                return childrenFromSnapshot(dataSnapshot);
            }
        }

        else if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.BThreadPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUser and thread");
            String threadFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(threadFirebaseID))
            {
                return getThreadForUser(dataSnapshot, threadFirebaseID, userFirebaseID);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Follower Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.BFollowers))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUsersPath and BFollowers");
            String followerFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(followerFirebaseID))
            {
                return getFollower(dataSnapshot, userFirebaseID, followerFirebaseID, BFollower.Type.FOLLOWER);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Follower Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.BFollows))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUsersPath and BFollows");
            String followerFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(followerFirebaseID))
            {
                return getFollower(dataSnapshot, userFirebaseID, followerFirebaseID, BFollower.Type.FOLLOWS);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Metadata Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.BMetaPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUser and metadata");
            String metaFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(metaFirebaseID))
            {
                Map<String, Object> values = (Map<String, Object>) dataSnapshot.getValue();

                // Get the user
                BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);

                /* Due to the name metadata that is added when the user is fetched when he is in a thread with the current thread.(getUserForThread)
                *  we need to get the metadata by his key and not by the entity id.*/

                  // Check that the metadata has a key so we can find the old metadata of the user if exist
                String key ="";
                int type = 0;
                if (values.containsKey(BDefines.Keys.BKey) && !values.get(BDefines.Keys.BKey).equals(""))
                    key = (String) values.get(BDefines.Keys.BKey);
                if (values.containsKey(BDefines.Keys.BType))
                {
                    type = ((Long) values.get(BDefines.Keys.BType)).intValue();
                }

                BMetadata metadata = user.fetchOrCreateMetadataForKey(key, type);

                // Update
                metadata.setEntityID(metaFirebaseID);
                metadata.setOwnerID(user.getId());
                metadata.updateFromMap(values);
                metadata = DaoCore.updateEntity(metadata);

                return metadata;
            } else return childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Thread Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath) || path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath, BFirebaseDefines.Path.BDetailsPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BThread and ThreadDetails");
            String threadFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(threadFirebaseID))
            {
                return getThread(dataSnapshot, threadFirebaseID);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BPublicThreadPath)){
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, Public BThread");
            String threadFirebaseID = path.idForIndex(0);
            if (StringUtils.isNotEmpty(threadFirebaseID))
            {
                BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
                thread.setEntityID(threadFirebaseID);
                thread.setType(BThread.Type.Public);
                DaoCore.updateEntity(thread);
                // ASK do i need to load messgaes and more stuff?
                return thread;
            } else return childrenFromSnapshot(dataSnapshot);
        }
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath, BFirebaseDefines.Path.BUsersPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BThread and BUser");
            // In this case we're storing the facebook ID instead of the entity ID
            // The reason for this is that when we access the friends list we get
            // hold of the FacebookID rather than the firebase ID. We don't really
            // need the user's FirebaseID anyway
            String userFirebaseID = path.idForIndex(1);
            String threadFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(userFirebaseID))
            {
                return getUserForThread(dataSnapshot, userFirebaseID, threadFirebaseID);
            }
            else childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Message Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath, BFirebaseDefines.Path.BMessagesPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BThread and BMessage");
            String messageFirebaseID = path.idForIndex(1);
            String threadFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(messageFirebaseID))
            {
                return getMessage(dataSnapshot, messageFirebaseID, threadFirebaseID);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }


        return null;
    }

    public static Object[] childrenFromSnapshot(DataSnapshot dataSnapshot){
        if (DEBUG) Log.v(TAG, "childrenFromSnapshot");
        Object children[] = new Object[(int) dataSnapshot.getChildrenCount()];

        int count = 0;
        for(DataSnapshot o :dataSnapshot.getChildren())
        {
            children[count] = objectFromSnapshot(o);
            count++;
        }

        return children;
    }

    private static class GetUserCall implements Callable<BUser> {

        private Map<String, Object> values;
        private String userFirebaseID;
        private DataSnapshot snapshot;

        private GetUserCall(DataSnapshot snapshot, Map<String, Object> values, String userFirebaseID) {
            this.values = values;
            this.userFirebaseID = userFirebaseID;
            this.snapshot = snapshot;
        }

        @Override
        public BUser call() throws Exception {
            // We need this in case we found the user first using the authID (so we don't know the Firebase ID)
            String authID = (String) values.get(BDefines.Keys.BAuthenticationID);
            BUser user = DaoCore.fetchOrCreateUserWithEntityAndAutID(userFirebaseID, authID);

            if (user == null)
            {
                if (DEBUG) Log.e(TAG, "Entity from DB is null");
                return null;
            }

            user.setEntityID(userFirebaseID);

            user.updateFromMap(values);

            // Updating the user in the database.
            user = DaoCore.updateEntity(user);
       /* Note to much is going on on start up this is a fix.// We only want to check the threads if this is the current user
        if (user.equals(BNetworkManager.sharedManager().getNetworkAdapter().currentUser()))
        {
          if (DEBUG) Log.d(TAG, "Checking user threads.");
           objectFromSnapshot(snapshot.child(BFirebaseDefines.Path.BThreadPath));
        }*/

            // Get more children if has any.
            objectFromSnapshot(snapshot.child(BFirebaseDefines.Path.BMetaPath));

            return user;
        }
    }

    /** This is currently not used. all the logic for thread users is in the {@link com.braunster.androidchatsdk.firebaseplugin.firebase.listeners.UserAddedToThreadListener UserAddedToThreadListener}.*/
    private static class GetUserForThreadCall implements Callable<BUser>{
        private String userFirebaseID, threadFirebaseID;
        private Map<String, Object> values;

        private GetUserForThreadCall(String userFirebaseID, String threadFirebaseID, Map<String, Object> values) {
            this.userFirebaseID = userFirebaseID;
            this.threadFirebaseID = threadFirebaseID;
            this.values = values;
        }

        @Override
        public BUser call() throws Exception {
            BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);
            BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);

            user.setEntityID(userFirebaseID);
            thread.setEntityID(threadFirebaseID);

            // The name contained in the thread isn't very reliable because
            // it's a duplicate so we only update the user if they don't
            // have a name set
            String name = (String) values.get(BDefines.Keys.BName);
            if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getMetaName()))
                user.setMetaName(name);

            if (userFirebaseID.equals(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()))
            {
                if (values.containsKey(BDefines.Keys.BDeleted))
                {
                    thread.setDeleted(true);
                    DaoCore.updateEntity(thread);
                }
            }

            // Saving the name for the user.
            DaoCore.updateEntity(user);

            // If the user has a leaved value that means he left the thread,
            // In that case we unlink the user and thread.
            if (values.containsKey(BDefines.Keys.BLeaved))
            {
                if (DEBUG) Log.d(TAG, "User lef thread");
                BLinkData data =
                        DaoCore.fetchEntityWithProperties(com.braunster.chatsdk.dao.BLinkData.class,
                                new Property[]{BLinkDataDao.Properties.ThreadID, BLinkDataDao.Properties.UserID}, thread.getId(), user.getId());

                if (data != null)
                {
                    DaoCore.deleteEntity(data);
                }
            }
            else if (!thread.hasUser(user)){
                if (DEBUG) Log.d(TAG, "Thread doesn't contain user");
                DaoCore.connectUserAndThread(user, thread);
            }
            return user;
        }
    }

    private static class GetThreadCall implements Callable<BThread>{
        private String threadFirebaseID;
        private Map<String, Object> values;

        private GetThreadCall(String threadFirebaseID, Map<String, Object> values) {
            this.threadFirebaseID = threadFirebaseID;
            this.values = values;
        }

        @Override
        public BThread call() throws Exception {

            BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
            thread.setEntityID(threadFirebaseID);

            thread.updateFromMap(values);

            thread.setLastUpdated(new Date());

            DaoCore.updateEntity(thread);

            return thread;
        }
    }

    private static class GetThreadForUserCall implements Callable<BThread>{

        private String threadFirebaseID, userFirebaeID;

        private GetThreadForUserCall(String threadFirebaseID, String userFirebaeID) {
            this.threadFirebaseID = threadFirebaseID;
            this.userFirebaeID = userFirebaeID;
        }

        @Override
        public BThread call() throws Exception {
            BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
            thread.setEntityID(threadFirebaseID);
            thread.setType(3);// FIXME no type cause fails.

            BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaeID);
            user.setEntityID(userFirebaeID);

            // If the user and the thread does not have a bond with each other bind them.
            if (!user.hasThread(thread))
            {
                if (DEBUG) Log.d(TAG, "User doesn't contain thread.");
                DaoCore.connectUserAndThread(user, thread);
            }

            thread.setLastUpdated(new Date());
            return thread;
        }
    }

    private static class GetFollowerCall implements Callable<BFollower>{
        private String userEntityId, followerEntityId;
        private int type = -1;

        private GetFollowerCall(String userEntityId, String followerEntityId, int type) {
            this.userEntityId = userEntityId;
            this.followerEntityId = followerEntityId;
            this.type = type;
        }

        @Override
        public BFollower call() throws Exception {
            BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userEntityId);
            BUser followerUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followerEntityId);
            return user.fetchOrCreateFollower(followerUser, type);
        }
    }

    private static class GetMessageCall implements Callable<BMessage>{

        private String messageFirebaseID, threadFirebaseID;
        private Map<String, Object> values;

        private GetMessageCall(String messageFirebaseID, String threadFirebaseID, Map<String, Object> values) {
            this.messageFirebaseID = messageFirebaseID;
            this.threadFirebaseID = threadFirebaseID;
            this.values = values;
        }

        @Override
        public BMessage call() throws Exception {

            BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
            thread.setEntityID(threadFirebaseID);

            // Checking to see if this thread was deleted.
            if (thread.isDeleted())
            {
                if (DEBUG) Log.v(TAG, "Thread is Deleted");

                // Setting it to be no more deleted
                thread.setDeleted(false);

                // Removing the deleted value from firebase.
                Firebase threadUserRef = FirebasePaths.threadRef(threadFirebaseID)
                        .appendPathComponent(BFirebaseDefines.Path.BUsersPath)
                        .appendPathComponent(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID())
                        .appendPathComponent(BDefines.Keys.BDeleted);

                threadUserRef.removeValue();
            }

            BMessage message = DaoCore.fetchOrCreateEntityWithEntityID(BMessage.class, messageFirebaseID);
            message.setEntityID(messageFirebaseID);

            String payload = (String) values.get(BDefines.Keys.BPayload);
            if (StringUtils.isNotEmpty(payload))
                message.setText(payload);

            Long messageType = (Long) values.get(BDefines.Keys.BType);
            if (messageType != null)
                message.setType(messageType.intValue());

            Long date = null;
            try {
                date = (Long) values.get(BDefines.Keys.BDate);
            } catch (ClassCastException e) {
                date = (((Double) values.get(BDefines.Keys.BDate)).longValue());
//            e.printStackTrace();
            }
            finally {
                if (date != null)
                    message.setDate(new Date(date));
            }

            String userFirebaseID = (String) values.get(BDefines.Keys.BUserFirebaseId);
            if (StringUtils.isNotEmpty(userFirebaseID))
            {
                BUser sender = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);
                message.setBUserSender(sender);
            }

            String color = (String) values.get(BDefines.Keys.BColor);
            if (StringUtils.isNotEmpty(color))
                message.color = color;

            String textColor = (String) values.get(BDefines.Keys.BTextColor);
            if (StringUtils.isNotEmpty(textColor))
                message.textColor = textColor;

            String fontName = (String) values.get(BDefines.Keys.BFontName);
            if (StringUtils.isNotEmpty(fontName))
                message.fontName = fontName;

            Integer fontSize = (Integer) values.get(BDefines.Keys.BFontSize);
            if (fontSize != null)
                message.fontSize = fontSize;

            message.setLastUpdated(new Date());

            // Mark the thead as having unread messages if this message
            // doesn't already exist on the thread
            if (message.getBThreadOwner() == null)
                thread.setHasUnreadMessages(true);

            // Update the thread and message
            DaoCore.updateEntity(thread);

            // Update the message.
            message.setBThreadOwner(thread);
            message.setOwnerThread(thread.getId());
            DaoCore.updateEntity(message);

            return message;
        }
    }

    private static BUser getUser(DataSnapshot snapshot, String userFirebaseID){
        if (DEBUG) Log.v(TAG, "getUser");
        try {
            return DaoCore.daoSession.callInTx(new GetUserCall(snapshot, (Map<String, Object>) snapshot.getValue(), userFirebaseID));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Get user call exception, message: "  + e.getMessage());
            return null;
        }
    }

    private static BUser getUserForThread(DataSnapshot snapshot, String userFirebaseID, String threadFirebaseID){
        if (DEBUG) Log.v(TAG, "getUserForThread");
        try {
            return DaoCore.daoSession.callInTx(new GetUserForThreadCall(userFirebaseID, threadFirebaseID, (Map<String, Object>) snapshot.getValue()));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Get user for thread call exception, message: "  + e.getMessage());
            return null;
        }
    }

    private static BThread getThread(DataSnapshot snapshot, String threadFirebaseID){
        if (DEBUG) Log.v(TAG, "getThread, ID: " + threadFirebaseID);
        try {
            return DaoCore.daoSession.callInTx(new GetThreadCall(threadFirebaseID, (Map<String, Object>) snapshot.getValue()));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, " get thread call exception, message: " + e.getMessage());
            return null;
        }
    }

    private static BThread getThreadForUser(DataSnapshot snapshot, String threadFirebaseID, String userFirebaeID){
        if (DEBUG) Log.v(TAG, "getThreadForUser");
        try {
            return DaoCore.daoSession.callInTx(new GetThreadForUserCall(threadFirebaseID, userFirebaeID));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "get thread for user exception, message: " + e.getMessage());
            return null;
        }
    }

    private static BMessage getMessage(DataSnapshot snapshot, String messageFirebaseID, String threadFirebaseID) {
        if (DEBUG) Log.v(TAG, "getMessage");
        try {
            return DaoCore.daoSession.callInTx(new GetMessageCall(messageFirebaseID, threadFirebaseID, (Map<String, Object>) snapshot.getValue()));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "get message call exception, message: " + e.getMessage());
            return null;
        }
    }

    private static BFollower getFollower(DataSnapshot snapshot, String userFirebaseId, String followerFirebaseId, int followerType){
        if (DEBUG) Log.v(TAG, "getFollower");
        try {
            return DaoCore.daoSession.callInTx(new GetFollowerCall(userFirebaseId, followerFirebaseId, followerType));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "get follower call exception, follower: " + e.getMessage());
            return null;
        }
    }
}








