package com.braunster.chatsdk.network.firebase;

import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.core.Entity;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
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


/* *
Created by itzik on 6/8/2014.
*/


public class BFirebaseInterface {

    private static final String TAG = BFirebaseInterface.class.getSimpleName();
    private static boolean DEBUG = true;

    //region Old push Entity
   /* public void pushEntity(final Entity entity, final CompletionListenerWithData completionListener){

        if (entity == null)
        {
            Log.e(TAG, "Entity is null");
            return;
        }

        selectEntity(entity, new CompletionListenerWithData<Entity>() {
            @Override
            public void onDone(Entity e) {

                if (e != null) {
                    entity.updateFrom(e);
                }

                FirebasePaths ref = FirebasePaths.firebaseRef();
                if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());
                ref = ref.appendPathComponent(entity.getPath().getPath());
                if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());

                String priority = "";

                Ask if think this is needed in android or just check for null,
                if ([entity respondsToSelector:@selector(getPriority)]) {
                    priority = entity.getPriority;
                }


                // If the entity has id that means its already added to the database and only need to be updated.
                if (entity.getEntityID() != null && entity.getEntityID().length() > 0)
                {
                    ref.updateChildren(entity.asMap(), new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError == null)
                                completionListener.onDone(entity);
                            else
                            {
                                if (DEBUG) Log.e(TAG, "Error while updating entity children");
                                completionListener.onDoneWithError();
                            }
                        }
                    });
                }
                else
                {
                    if (DEBUG) Log.d(TAG, "Selected Entity is null, Creating Entity...");
                    // Pushing a new child to the list.
                    Firebase listRef = ref.push();
                    // Set the entity id to the new child added name.
                    entity.setEntityId(listRef.getName());
//                    listRef.setPriority(entity.getPriority());
                    listRef.setValue(entity.asMap(), entity.getPriority(), new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (DEBUG) Log.d(TAG, "Entity is pushed.");
                            completionListener.onDone(entity);
                        }
                    });
                }
            }

            @Override
            public void onDoneWithError() {

            }
        });
    }*/
    //endregion

    public static void pushEntity(final Entity entity, final RepetitiveCompletionListenerWithError listener){
        if (DEBUG){
            Log.v(TAG, "pushEntity");
            DaoCore.printEntity(entity);
        }

        // We're going to be pushing an entity which may have sub entities
        // we need to call status for each item that completes then call
        // completion when they all complete
        Collection<Entity> entitiesToPush = new ArrayList<Entity>();

        // Adding child entities if has any.
        if (entity.getChildren() != null && entity.getChildren().size() > 0)
        {
            if (DEBUG) Log.d(TAG, "Entity has children. Amount: " + entity.getChildren().size());
//            Collections.addAll(entity.getChildren(), entitiesToPush);
            entitiesToPush.addAll(entity.getChildren());
            if (DEBUG) Log.d(TAG, "entitiesToPush: " + entitiesToPush.size());
        }
        // Adding the parent entity after so it will be the first to be added.
        entitiesToPush.add(entity);

        PushCompletedListener comListener = new PushCompletedListener(listener, entitiesToPush.size());

        for (Entity e : entitiesToPush){
            implPushEntity(e, comListener);

            // The onItem method is returning a boolean value that indicated if the caller want to continue pushing entities.
             if(comListener.isStoped())
                 break;
        }
    }

    /** For each item that is finsihed his push a method in this class will called. if the caller method returns true the pushing will be stopped.*/
    public static class PushCompletedListener implements CompletionListenerWithDataAndError<Entity, FirebaseError>{
        /** Keep the value returned from <b>onItem</b>, If value is false we stop the pushing.
         * Keep in mind that the actual stop is made by the loop on the <b>pushEntity</b> method.*/
        public boolean stop = false;
        /** The amount of entities that needs to be pushed, We need this so we know when all the pushes is done and we can trigger <b>onDone</b>.*/
        private int pushesAmount = 0;
        private RepetitiveCompletionListenerWithError listener;

        public PushCompletedListener(RepetitiveCompletionListenerWithError listener, int pushesAmount){
            this.listener = listener;
            this.pushesAmount = pushesAmount;
        }

        @Override
        public void onDone(Entity entity) {
            pushesAmount--;
            stop = listener.onItem(entity);

            if (pushesAmount <= 0)
                listener.onDone();
        }

        @Override
        public void onDoneWithError(Entity entity, FirebaseError error) {
            listener.onItemError(entity, error);
        }

        public boolean isStoped() {
            return stop;
        }
    }

    public static void implPushEntity(final Entity entity, final CompletionListenerWithDataAndError<Entity, FirebaseError> listener){

        // ASK what dirty means? and if it should be saved on the db.
//        if (entity.dirty){
        Object priority = entity.getPriority();

        if (entity.getEntityID() == null && entity.getPriority() != null){
            selectEntity(entity, new CompletionListenerWithDataAndError<Entity, FirebaseError>() {
                @Override
                public void onDone(Entity en) {
                    try {
                        // ASK i think this is the right behavior wanted
                        // Call the method to push the entity,
                        // If entity selected is null use the entity given in the "impl_pushEntity" call.
                        new pushEntity( (en==null?entity:en) , listener).call();
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onDoneWithError(entity, null);
                    }
                }

                @Override
                public void onDoneWithError(Entity en, FirebaseError error) {
                    listener.onDoneWithError(en, error);
                }
            });
        }
        else  try {
            // ASK i think this is the right behavior wanted
            // Call the method to push the entity,
            // If entity selected is null use the entity given in the "impl_pushEntity" call.
            new pushEntity( (entity) , listener).call();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onDoneWithError(entity, null);
        }
//        }
    }

    private static class pushEntity implements Callable{

        private Entity entity;
        private CompletionListenerWithDataAndError<Entity, FirebaseError> listener;

        public pushEntity(Entity entity, CompletionListenerWithDataAndError<Entity, FirebaseError> listener){
            this.entity = entity;
            this.listener = listener;
        }

        @Override
        public Void call() throws Exception {
            if (entity.getPath() == null)
            {
                if (DEBUG) Log.e(TAG, "Cant get path from entity");
                listener.onDoneWithError(entity, null);
                return null;
            }

            FirebasePaths ref = FirebasePaths.firebaseRef();
            if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());
            ref = ref.appendPathComponent(entity.getPath().getPath());
            if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());

            // If the entity has id that means its already added to the database and only need to be updated.
            if (entity.getEntityID() != null && entity.getEntityID().length() > 0)
            {
                ref.updateChildren(entity.asMap(), new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError == null)
                            listener.onDone(entity);
                        else
                        {
                            if (DEBUG) Log.e(TAG, "Error while updating entity children");
                            listener.onDoneWithError(entity, firebaseError);
                        }
                    }
                });
            }
            else
            {
                if (DEBUG) Log.d(TAG, "Selected Entity is null, Creating Entity...");
                // Pushing a new child to the list.
                Firebase listRef = ref.push();
                // Set the entity id to the new child added name.
                entity.setEntityId(listRef.getName());

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
                            // ASK i think this is the right behavior wanted
                            listener.onDone(entity);
                        }
                        else listener.onDoneWithError(entity, firebaseError);
                    }
                });
            }

            return null;
        }
    }

    /** Get entity from the firebase server.*/
    public static void selectEntity(final Entity entity, final CompletionListenerWithDataAndError listener){

        if (entity.getPath() == null)
        {
            if (DEBUG) Log.e(TAG, "Cant push entity with no path");
            listener.onDoneWithError(entity, null);
        }

        FirebasePaths ref = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());
        ref = ref.appendPathComponent(entity.getPath().getPath());
        if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());

        // TODO handle this path object if needed.
//        ref.appendPathComponent(entity.getPath().toString());

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

                    listener.onDone(obj!=null?obj:entity);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (DEBUG) Log.e(TAG, "onCancelled");
                    listener.onDoneWithError(entity, firebaseError);
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
                // FIXME cast to the right priority
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
                            listener.onDone(objects.length > 0 ? (obj!=null?obj:entity) : entity);
                        }
                        else
                        {
                            if(DEBUG) Log.d(TAG, "No Children");
                            Object obj = objectFromSnapshot(dataSnapshot);
                            listener.onDone(obj!=null?obj:entity);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        if (DEBUG) Log.e(TAG, "onCancelled");
                        listener.onDoneWithError(entity, firebaseError);
                    }
                });
            }
            else
            {
                if (DEBUG) Log.d(TAG, "No priority.");
                listener.onDone(entity);//ASK output null or just send error.
            }
        }
    }


    //TODO
    public static void observerUser(BUser user){

    }

    // TODO
    public static void observeThread(BThread thread){

    }

    //TODO remove all the observed thread, Theay need to be saved to a static list.
    public static void removeAllObservers(){

    }

    //TODO
    public static void loadMoreMessagesForThread(BThread thread, int numOfMessages, CompletionListenerWithData<List<BMessage>> listener){

    }

    public static Object objectFromSnapshot(DataSnapshot dataSnapshot){
        if (dataSnapshot == null || dataSnapshot.getValue() == null)
        {
            if (DEBUG) Log.e(TAG, "objectFromSnapshot, Snapshot is null or Snapshot.getValues() is null.");
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
                BUser user = getUser(dataSnapshot, userFirebaseID);
                if (DEBUG) Log.i(TAG, "objectFromSnapshot, Returning BUser");
                return user;
            }
            else {
                if (DEBUG) Log.i(TAG, "objectFromSnapshot");
                childrenFromSnapshot(dataSnapshot);
            }
        }

        if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.BThreadPath))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUser and thread");
            String threadFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(threadFirebaseID))
            {
                BThread thread = getThreadForUser(dataSnapshot, threadFirebaseID, userFirebaseID);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Thread Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath) || path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath, BFirebaseDefines.Path.BDetailsPath))
        {
            String threadFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(threadFirebaseID))
            {
                BThread thread = getThread(dataSnapshot, threadFirebaseID);
                return thread;
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath, BFirebaseDefines.Path.BUsersPath))
        {
            // In this case we're storing the facebook ID instead of the entity ID
            // The reason for this is that when we access the friends list we get
            // hold of the FacebookID rather than the firebase ID. We don't really
            // need the user's FirebaseID anyway
            String userFirebaseID = path.idForIndex(1);
            String threadFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(userFirebaseID))
            {
                BUser user = getUserForThread(dataSnapshot, userFirebaseID, threadFirebaseID);
                return user;
            }
            else childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Message Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BThreadPath, BFirebaseDefines.Path.BMessagesPath))
        {
            String messageFirebaseID = path.idForIndex(1);
            String threadFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(messageFirebaseID))
            {
                BMessage message = getMessage(dataSnapshot, messageFirebaseID, threadFirebaseID);
                return message;
            }
            else childrenFromSnapshot(dataSnapshot);
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

    /* Get methods for the "objectFromSnapshot" Method.*/
    private static BUser getUser(DataSnapshot snapshot, String userFirebaseID){
        Map<String, Object> values = (Map<String, Object>) snapshot.getValue();
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

        // We only want to check the threads if this is the current user
        if (user.equals(BNetworkManager.sharedManager().getNetworkAdapter().currentUser()))
        {
           objectFromSnapshot(snapshot.child(BFirebaseDefines.Path.BThreadPath));
        }

        // Get more children if has any.
        objectFromSnapshot(snapshot.child(BFirebaseDefines.Path.BMetaPath));

        return user;
    }

    private static BUser getUserForThread(DataSnapshot snapshot, String userFirebaseID, String threadFirebaseID){
        Map<String, Object> values = (Map<String, Object>) snapshot.getValue();

        BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID);
        user.setEntityID(userFirebaseID);

        // The name contained in the thread isn't very reliable because
        // it's a duplicate so we only update the user if they don't
        // have a name set
        String name = (String) values.get(BDefines.Keys.BName);
        if (StringUtils.isNotEmpty(name) && StringUtils.isEmpty(user.getName()))
            user.setName(name);

        BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
        thread.setEntityID(threadFirebaseID);


        // check if the thread and user is already connected. Pretty sure that the dual check is not needed.
        /*boolean containsThread = false, containsUser = false;
        for (BLinkData linkData : user.getThreads())
        {
            if (linkData.getBThread().equals(thread))
                containsThread = true;
        }

        for (BLinkData linkData : thread.getUsers())
        {
            if (linkData.getBUser().equals(user))
                containsUser = true;
        }

        if (!containsThread  && !containsUser)
            DaoCore.connectUserAndThread(user, thread);*/

        return user;
    }

    private static BThread getThread(DataSnapshot snapshot, String threadFirebaseID){
        Map<String, Object> values = (Map<String, Object>) snapshot.getValue();

        BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
        thread.setEntityID(threadFirebaseID);

        thread.setLastUpdated(new Date());

        if (values.containsKey(com.braunster.chatsdk.network.BDefines.Keys.BCreationDate))
        {
            Long creationDate = (Long) values.get(com.braunster.chatsdk.network.BDefines.Keys.BCreationDate);
            if (creationDate != null)
                thread.setCreationDate(new Date(creationDate));
        }

        objectFromSnapshot( snapshot.child(BFirebaseDefines.Path.BMessagesPath));

        objectFromSnapshot( snapshot.child(BFirebaseDefines.Path.BUsersPath));

        return thread;
    }

    private static BThread getThreadForUser(DataSnapshot snapshot, String threadFirebaseID, String userFirebaeID){

        String value = (String) snapshot.getValue();

        BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
        thread.setEntityID(threadFirebaseID);

        BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userFirebaeID);
        user.setEntityID(userFirebaeID);

        // If the user and the thread does not have a bond with each other bind them.
        if (!user.getThreads().contains(thread))
            DaoCore.connectUserAndThread(user, thread);

        thread.setLastUpdated(new Date());

        // check if the thread and user is already connected. Pretty sure that the dual check is not needed.
        /*boolean containsThread = false, containsUser = false;
        for (BLinkData linkData : user.getThreads())
        {
            if (linkData.getBThread().equals(thread))
                containsThread = true;
        }

        if ()

        for (BLinkData linkData : thread.getUsers())
        {
            if (linkData.getBUser().equals(user))
                containsUser = true;
        }*/

        /*if (!containsThread  && !containsUser)
            DaoCore.connectUserAndThread(user, thread);
*/
        return thread;
    }

    private static BMessage getMessage(DataSnapshot snapshot, String messageFirebaseID, String threadFirebaseID)
    {
        Map<String, Object> values = (Map<String, Object>) snapshot.getValue();

        BMessage message = DaoCore.fetchEntityWithEntityID(BMessage.class, messageFirebaseID);
        message.setEntityID(messageFirebaseID);

        String payload = (String) values.get(com.braunster.chatsdk.network.BDefines.Keys.BPayload);
        if (StringUtils.isNotEmpty(payload))
            message.setText(payload);

        Integer messageType = (Integer) values.get(com.braunster.chatsdk.network.BDefines.Keys.BType);
        if (messageType != null)
            message.setType(messageType);

        Long date = (Long) values.get(com.braunster.chatsdk.network.BDefines.Keys.BDate);
        if (date != null)
            message.setDate(new Date(date));

        String userFirebaseID = (String) values.get(BDefines.Keys.BUserFirebaseId);
        if (StringUtils.isNotEmpty(userFirebaseID))
            message.setBUserSender(DaoCore.<BUser>fetchOrCreateEntityWithEntityID(BUser.class, userFirebaseID));

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

        BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadFirebaseID);
        thread.setEntityID(threadFirebaseID);

        // Mark the therad as having unread messages if this message
        // doesn't already exist on the thread
        if (message.getBThreadOwner() == null)
            thread.setHasUnreadMessages(true);

        message.setBThreadOwner(thread);

        return message;
    }
}
