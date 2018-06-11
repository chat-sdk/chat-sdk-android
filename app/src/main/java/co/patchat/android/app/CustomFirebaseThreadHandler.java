package co.patchat.android.app;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseThreadHandler;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class CustomFirebaseThreadHandler extends FirebaseThreadHandler {

    @Override
    protected Completable setUserThreadLinkValue(Thread thread, List<User> users, int userThreadLinkType) {
        return Completable.create(e -> {

            DatabaseReference ref = FirebasePaths.firebaseRawRef();
            final HashMap<String, Object> data = new HashMap<>();

            for (final User u : users) {

                DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(u.getEntityID()).child(Keys.Status);
                DatabaseReference userThreadsRef = FirebasePaths.userThreadsRef(u.getEntityID()).child(thread.getEntityID()).child(Keys.InvitedBy);

                String threadUsersPath = threadUsersRef.toString().replace(threadUsersRef.getRoot().toString(), "");
                String userThreadsPath = userThreadsRef.toString().replace(userThreadsRef.getRoot().toString(), "");

                //
                if(userThreadLinkType == UserThreadLinkTypeAddUser) {
                    data.put(threadUsersPath, u.getEntityID().equals(thread.getCreatorEntityId()) ? Keys.Owner : Keys.Member);
                    data.put(userThreadsPath, NM.currentUser().getEntityID());
                }
                else if (userThreadLinkType == UserThreadLinkTypeRemoveUser) {
                    data.put(threadUsersPath, null);
                    data.put(userThreadsPath, null);
                }
            }

            ref.updateChildren(data, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    FirebaseEntity.pushThreadUsersUpdated(thread.getEntityID()).subscribe(new CrashReportingCompletableObserver());
                    for(User u : users) {
                        FirebaseEntity.pushUserThreadsUpdated(u.getEntityID()).subscribe(new CrashReportingCompletableObserver());
                    }
                    e.onComplete();
                } else {
                    e.onError(databaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.single());
    }

}
