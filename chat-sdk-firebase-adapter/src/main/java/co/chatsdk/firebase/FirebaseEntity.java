package co.chatsdk.firebase;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import co.chatsdk.core.utils.StringChecker;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import timber.log.Timber;

/**
 * Created by ben on 9/11/17.
 */

public class FirebaseEntity {

    public static Completable pushThreadDetailsUpdated (String threadEntityID) {
        return pushUpdated(FirebasePaths.ThreadsPath, threadEntityID, FirebasePaths.DetailsPath);
    }

    public static Completable pushThreadUsersUpdated (String threadEntityID) {
        return pushUpdated(FirebasePaths.ThreadsPath, threadEntityID, FirebasePaths.UsersPath);
    }

    public static Completable pushThreadMessagesUpdated (String threadEntityID) {
        return pushUpdated(FirebasePaths.ThreadsPath, threadEntityID, FirebasePaths.MessagesPath);
    }

    public static Completable pushUserMetaUpdated (String threadEntityID) {
        return pushUpdated(FirebasePaths.UsersPath, threadEntityID, FirebasePaths.MetaPath);
    }

    public static Completable pushUserThreadsUpdated (String userEntityID) {
        return pushUpdated(FirebasePaths.UsersPath, userEntityID, FirebasePaths.ThreadsPath);
    }

    public static Completable pushUpdated (final String path, final String entityID, final String key) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                Timber.v(path +'/'+entityID+'/'+key);

                if(StringChecker.isNullOrEmpty(path) || StringChecker.isNullOrEmpty(entityID) || StringChecker.isNullOrEmpty(key)) {
                    e.onComplete();
                    return;
                }

                DatabaseReference ref = FirebasePaths.firebaseRef().child(path).child(entityID).child(FirebasePaths.UpdatedPath).child(key);
                ref.setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            e.onComplete();
                        }
                        else {
                            e.onError(databaseError.toException());
                        }
                    }
                });
            }
        });
    }

}
