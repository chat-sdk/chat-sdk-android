package com.braunster.androidchatsdk.firebaseplugin.firebase;

import co.chatsdk.core.NM;
import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.wrappers.ThreadWrapper;

import com.braunster.chatsdk.object.ChatError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

/**
 * Created by kykrueger on 2016-09-04.
 */
@Deprecated
public class ThreadRecovery {

    public static Single<BThread> checkForAndRecoverThreadWithUsers(List<BUser> users){
        return checkForRemoteThreadWithUsers(users).flatMap(new Function<String, SingleSource<BThread>>() {
            @Override
            public SingleSource<BThread> apply(final String s) throws Exception {
                ThreadWrapper threadWrapper = new ThreadWrapper(s);
                return threadWrapper.recoverPrivateThread();
            }
        });
    }

    public static Single<String> checkForRemoteThreadWithUsers(final List<BUser> users){
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(final SingleEmitter<String> e) throws Exception {

                final List<String> userEntityIds = new ArrayList<>();

                BUser currentUser = NM.currentUser();
                DatabaseReference userThreadsRef = FirebasePaths.userThreadsRef(currentUser.getEntityID());

                // Look through all of their associated threads for an existing one with the listed users
                // We don't need to worry about this linking to a public thread because the current
                // user cannot be in a public thread while trying to create a private one.
                userThreadsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange( DataSnapshot allThreadsForUser) {
                                // Check all of the user's threads (only the threadIds are here)
                                Boolean lastThread = false;
                                int threadNumber = 0;

                                if (allThreadsForUser.getChildrenCount() == 0) {
                                    e.onError(new ChatError(404, "Could not find exiting Thread"));
                                }
                                else {
                                    for(DataSnapshot threadOfUser : allThreadsForUser.getChildren()){
                                        threadNumber = threadNumber + 1;

                                        if(allThreadsForUser.getChildrenCount() == threadNumber) {
                                            lastThread = true;
                                        }
                                        final Boolean lastThreadFinal = lastThread;

                                        DatabaseReference threadRef = FirebasePaths.threadRef(threadOfUser.getKey());
                                        // Make a call to retrieve details for each thread
                                        threadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot thread) {
                                                Integer numberOfUsers = 0;
                                                Boolean threadFound = true;

                                                DataSnapshot threadUsersPath = thread
                                                        .child(FirebasePaths.UsersPath);

                                                for (DataSnapshot user : threadUsersPath.getChildren()){
                                                    numberOfUsers = numberOfUsers + 1;
                                                    if(!userEntityIds.contains(user.getKey())) threadFound = false;
                                                }
                                                // If there are no other users, and the number of users are the same
                                                // This is an existing thread between the specified users
                                                // Return if this is not true
                                                if(numberOfUsers != userEntityIds.size() || !threadFound){
                                                    if(lastThreadFinal){
                                                        e.onError(new ChatError(404, "Could not find exiting Thread"));
                                                    }
                                                    return;
                                                }

                                                e.onSuccess(thread.getKey());
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                e.onError(databaseError.toException());
                                            }
                                        });

                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                e.onError(databaseError.toException());
                            }
                        });
            }
        });
    }

}
