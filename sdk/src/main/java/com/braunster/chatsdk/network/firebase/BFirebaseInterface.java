package com.braunster.chatsdk.network.firebase;

import android.util.Log;

import com.braunster.chatsdk.activities.LoginActivity;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.core.Entity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Date;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 */
public class BFirebaseInterface {

    private static final String TAG = BFirebaseInterface.class.getSimpleName();
    private static boolean DEBUG = true;

    public static BFirebaseInterface getInstance(){
        return new BFirebaseInterface();
    }

    private BFirebaseInterface(){

    }

    public void pushEntity(final Entity entity, final CompletionListenerWithData completionListener){

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
                if (DEBUG) Log.d(TAG, "PushEntity, RefPath: " + ref.toString());
                ref = ref.appendPathComponent(FirebaseTags.BUsersPath );
                if (DEBUG) Log.d(TAG, "PushEntity, RefPath: " + ref.toString());

                String priority = "";

                /* Ask if think this is needed in android or just check for null,
                if ([entity respondsToSelector:@selector(getPriority)]) {
                    priority = entity.getPriority;
                }*/

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
                    // Pushing a new child to the list.
                    Firebase listRef = ref.push();
                    // Set the entity id to the new child added name.
                    entity.setEntityId(listRef.getName());
                    listRef.setPriority(entity.getPriority());
                    listRef.setValue(entity.asMap(), new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            completionListener.onDone(entity);
                        }
                    });
                }
            }

            @Override
            public void onDoneWithError() {

            }
        });
    }

    /** Get entity from the firebase server.*/
    public void selectEntity(Entity entity, final CompletionListenerWithData completionListenerWithData){
        FirebasePaths ref = FirebasePaths.firebaseRef();
        if (DEBUG) Log.d(TAG, "SelectEntity, RefPath: " + ref.toString());
        ref = ref.appendPathComponent(FirebaseTags.BUsersPath);
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

                    completionListenerWithData.onDone(obj);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (DEBUG) Log.e(TAG, "onCancelled");
                    completionListenerWithData.onDoneWithError();
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
                ref.startAt(String.valueOf(entity.getPriority())).endAt(String.valueOf(entity.getPriority())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (DEBUG) Log.v(TAG, "onDataChanged");
                        if (dataSnapshot.hasChildren())
                        {
                            if(DEBUG) Log.d(TAG, "HasChildren");
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                completionListenerWithData.onDone(objectFromSnapshot(d));
                        }
                        else
                        {
                            if(DEBUG) Log.d(TAG, "No Children");
                            completionListenerWithData.onDone(objectFromSnapshot(dataSnapshot));//ASK output null or just send error.
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        if (DEBUG) Log.e(TAG, "onCancelled");
                        completionListenerWithData.onDoneWithError();
                    }
                });
                if (DEBUG) Log.d(TAG, "Getting entity by priority");
            }
            else
            {
                if (DEBUG) Log.d(TAG, "No priority.");
                completionListenerWithData.onDone(null);//ASK output null or just send error.
            }
        }
    }

    // TODO start parsing objects.
    public Object objectFromSnapshot(DataSnapshot dataSnapshot){
        if (DEBUG)Log.v(TAG, "objectFromSnapshot, Path: " + dataSnapshot.getRef().getPath().toString());
        BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());

        Map<String, Object> values = (Map<String, Object>) dataSnapshot.getValue();

        // ---------------
        // User class type
        if (path.isEqualToComponent(new String[]{FirebaseTags.BUsersPath}))
        {
            String userFirebaseID = path.idForIndex(0);

            if (userFirebaseID != null)
            {
                BUser user = DaoCore.fetchOrCreateUserWithEntityID(BUser.class, userFirebaseID);

                if (user == null)
                {
                    if (DEBUG) Log.e(TAG, "Entity from DB is null");
                    return null;
                }

                user.setEntityID(userFirebaseID);
                String facebookID = (String) values.get(FirebaseTags.BFacebookID);
                if (facebookID != null)
                    user.setFacebookID(facebookID);

                String name = (String) values.get(FirebaseTags.BName);

                if (name != null)
                    user.setName(name);

                Boolean pictureExist = (Boolean) values.get(FirebaseTags.BPictureExists);

                if (pictureExist != null)
                    user.pictureExist = pictureExist;


                String pictureURL = (String) values.get(FirebaseTags.BPictureURL);

                if (pictureURL != null)
                    user.pictureURL = pictureURL;

                user.setLastUpdated(new Date());

                //TODO get children if has any.

                return user;
            }
            else childrenFromSnapshot(dataSnapshot);
        }

        return null;
    }

    public Object[] childrenFromSnapshot(DataSnapshot dataSnapshot){
        Object children[] = new Object[(int) dataSnapshot.getChildrenCount()];

        int count = 0;
        for(Object o :dataSnapshot.getChildren())
        {
            children[count] = objectFromSnapshot(dataSnapshot);
            count++;
        }

        return children;
    }
}
