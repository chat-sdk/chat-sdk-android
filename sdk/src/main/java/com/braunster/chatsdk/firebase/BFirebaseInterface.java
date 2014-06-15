package com.braunster.chatsdk.firebase;

import android.util.Log;

import com.braunster.chatsdk.activities.LoginActivity;
import com.braunster.chatsdk.dao.AbstractEntity;
import com.braunster.chatsdk.dao.Entity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Objects;

/**
 * Created by itzik on 6/8/2014.
 */
public class BFirebaseInterface {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    public void pushEntity(AbstractEntity entity, final CompletionListener completionListener){

        selectEntity(entity, new CompletionListenerWithData<Entity>() {
            @Override
            public void onDone(Entity e) {
                final Entity entity = e;
                entity.updateFrom(e);

                Firebase ref = FirebasePaths.appendPathComponent(entity.getPath().toString());
                String priority = "";

                /* Ask if think this is needed in andoid or just check for null,
                if ([entity respondsToSelector:@selector(getPriority)]) {
                    priority = entity.getPriority;
                }*/

                // If the entity has id that means its already added to the database and only need to be updated.
                if (entity.entityID != null && entity.entityID.length() > 0)
                {
                    ref.updateChildren(entity.asMap(), new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError == null)
                                completionListener.onDone();
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
                    entity.entityID = listRef.getName();
                    listRef.setPriority(priority);
                    listRef.setValue(entity.asMap(), new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            completionListener.onDone();
                        }
                    });
                }
            }

            @Override
            public void onDoneWithError() {

            }
        });
    }

    public void selectEntity(AbstractEntity entity, final CompletionListenerWithData<Entity> completionListenerWithData){
        Firebase ref = FirebasePaths.appendPathComponent(entity.getPath().toString());

        if (entity.entityID != null && entity.entityID.length() > 0)
        {
            // Get the object by its address, If exists.
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Object obj = objectFromSnapshot(dataSnapshot);

                    if (obj  instanceof Object[])
                        obj = ((Object[])obj)[0];

                    completionListenerWithData.onDone((Entity) obj);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
        // If no address exists get the object by its priority.
        else
        {
            // Check if has priority
            if (entity.priority != null && !entity.priority.equals(""))
            {
                // FIXME cast to the right priority
                ref.startAt(String.valueOf(entity.priority)).endAt(String.valueOf(entity.priority)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren())
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                completionListenerWithData.onDone((Entity) objectFromSnapshot(d));
                        else
                            completionListenerWithData.onDone(null);//ASK output null or just send error.
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
            else
                completionListenerWithData.onDone(null);//ASK output null or just send error.
        }
    }

    // TODO start parsing objects.
    public Object objectFromSnapshot(DataSnapshot dataSnapshot){
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
