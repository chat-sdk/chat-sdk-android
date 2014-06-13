package com.braunster.chatsdk.firebase;

import android.util.Log;

import com.braunster.chatsdk.activities.LoginActivity;
import com.braunster.chatsdk.entities.Entity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by itzik on 6/8/2014.
 */
public class BFirebaseInterface {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    public void pushEntity(Entity entity, final CompletionListener completionListener){

        selectEntity(entity, new CompletionListenerWithData<Object>() {
            @Override
            public void onDone(Object e) {
                final Entity entity = (Entity) e;
                entity.updatedFrom(entity);

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

    public void selectEntity(Entity entity, final CompletionListenerWithData<Object> completionListenerWithData){
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

                    completionListenerWithData.onDone(obj);
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
            if (entity.priority != null)
            {
                ref.startAt(String.valueOf(entity.priority)).endAt(entity.priority).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren())
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                completionListenerWithData.onDone(objectFromSnapshot(d));
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
}
