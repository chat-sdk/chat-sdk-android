package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BThreadWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.object.BError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kykrueger on 2016-09-04.
 */
public class ThreadRecovery {
    public static Promise<BThread, BError, Void> checkForAndRecoverThreadWithUsers(List<BUser> users){

        final Deferred<BThread, BError, Void> deferred = new DeferredObject<>();

        checkForRemoteThreadWithUsers(users).done(new DoneCallback<String>() {
            @Override
            public void onDone(String foundRemoteThread) {
                BThreadWrapper threadWrapper = new BThreadWrapper(foundRemoteThread);
                threadWrapper.recoverPrivateThread().done(new DoneCallback<BThread>() {
                    @Override
                    public void onDone(BThread bThread) {
                        deferred.resolve(bThread);
                    }
                }).fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError bError) {
                        deferred.reject(bError);
                    }
                });
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                deferred.reject(bError);
            }
        });

        return deferred.promise();
    }

    public static Promise<String, BError, Void> checkForRemoteThreadWithUsers(List<BUser> users){
        final Deferred<String, BError, Void> deferred = new DeferredObject<>();
        final List<String> userEntityIds = new ArrayList<>();

        DatabaseReference currentUsersDatabasePath = null;

        // Get current user's firebase reference
        for ( BUser user : users){
            userEntityIds.add(user.getEntityID());
            if(user.isMe()){
                currentUsersDatabasePath = BUserWrapper.initWithModel(user).ref();
            }
        }

        // Look through all of their associated threads for an existing one with the listed users
        // We don't need to worry about this linking to a public thread because the current
        // user cannot be in a public thread while trying to create a private one.
        currentUsersDatabasePath.
                child(BFirebaseDefines.Path.BThreadPath).
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot allThreadsForUser) {
                // Check all of the user's threads (only the threadIds are here)
                Boolean lastThread = false;
                int threadNumber = 0;

                if (allThreadsForUser.getChildrenCount() == 0) {
                    deferred.reject(new BError(404, "Could not find existing Thread"));
                }

                for(DataSnapshot threadOfUser : allThreadsForUser.getChildren()){
                    threadNumber = threadNumber + 1;

                    // Stop searching if the thread has already been found
                    if (deferred.isResolved()) break;

                    if(allThreadsForUser.getChildrenCount() == threadNumber) lastThread = true;
                    final Boolean lastThreadFinal = lastThread;

                    DatabaseReference threadRef = FirebasePaths.threadRef(threadOfUser.getKey());
                    // Make a call to retrieve details for each thread
                    threadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot thread) {
                            Integer numberOfUsers = 0;
                            Boolean threadFound = true;

                            DataSnapshot threadUsersPath = thread
                                    .child(BFirebaseDefines.Path.BUsersPath);

                            for (DataSnapshot user : threadUsersPath.getChildren()){
                                numberOfUsers = numberOfUsers + 1;
                                if(!userEntityIds.contains(user.getKey())) threadFound = false;
                            }
                            // If there are no other users, and the number of users are the same
                            // This is an existing thread between the specified users
                            // Return if this is not true
                            if(numberOfUsers != userEntityIds.size() || !threadFound){
                                if(lastThreadFinal && deferred.isPending()){
                                    deferred.reject(new BError(404, "Could not find existing Thread"));
                                }
                                return;
                            }

                            // Return thread's entityId
                            deferred.resolve(thread.getKey());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (deferred.isPending()){
                                deferred.reject(new BError(404, "Could not find existing Thread"));
                            }
                        }
                    });

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (deferred.isPending()){
                    deferred.reject(new BError(404, "Could not find existing Thread"));
                }
            }
        });


        return deferred.promise();
    }

}
