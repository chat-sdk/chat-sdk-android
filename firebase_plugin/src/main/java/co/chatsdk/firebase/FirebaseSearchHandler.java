package co.chatsdk.firebase;

import com.braunster.chatsdk.object.ChatError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.dao.core.DaoDefines;
import co.chatsdk.core.defines.FirebaseDefines;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebaseSearchHandler implements SearchHandler {

    /** Indexing
     * To allow searching we're going to implement a simple index. Strings can be registered and
     * associated with users i.e. if there's a user called John Smith we could make a new index
     * like this:
     *
     * indexes/[index ID (priority is: johnsmith)]/[entity ID of John Smith]
     *
     * This will allow us to find the user*/
    @Override
    public Observable<BUser> usersForIndex(final String index, final String value) {
        return Observable.create(new ObservableOnSubscribe<BUser>() {
            @Override
            public void subscribe(final ObservableEmitter<BUser> e) throws Exception {
                if (StringUtils.isBlank(value))
                {
                    e.onError(ChatError.getError(ChatError.Code.NULL, "Value is blank"));
                    return;
                }

                Query query = FirebasePaths.indexRef().orderByChild(index).startAt(
                        processForQuery(value)).limitToFirst(FirebaseDefines.NumberOfUserToLoadForIndex);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {

                            Map<String, Objects> values = (Map<String, Objects>) snapshot.getValue();

                            final List<BUser> usersToGo = new ArrayList<BUser>();
                            List<String> keys = new ArrayList<String>();

                            // So we dont have to call the db for each key.
                            String currentUserEntityID = NetworkManager.shared().a.core.currentUserModel().getEntityID();

                            // Adding all keys to the list, Except the current user key.
                            for (String key : values.keySet())
                                if (!key.equals(currentUserEntityID))
                                    keys.add(key);

                            // Fetch or create users in the local db.
                            BUser bUser;
                            if (keys.size() > 0) {
                                for (String entityID : keys) {
                                    // Making sure that we wont try to get users with a null object id in the index section
                                    // If we will try the query will never end and there would be no result from the index.
                                    if(StringUtils.isNotBlank(entityID) && !entityID.equals(DaoDefines.Keys.Null) && !entityID.equals("(null)"))
                                    {
                                        bUser = StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, entityID);
                                        usersToGo.add(bUser);
                                    }
                                }

                                ArrayList<Completable> completables = new ArrayList<>();

                                for (final BUser user : usersToGo) {

                                    completables.add(UserWrapper.initWithModel(user).once().andThen(new CompletableSource() {
                                        @Override
                                        public void subscribe(final CompletableObserver cs) {

                                            // Notify that a user has been found.
                                            // Making sure the user due start with the wanted name
                                            if (processForQuery(user.metaStringForKey(index)).startsWith(processForQuery(value))) {
                                                cs.onComplete();
                                            }
                                            else {

                                                // Remove the not valid user from the list.
                                                usersToGo.remove(user);
                                                cs.onComplete();
                                            }
                                        }
                                    }).doOnComplete(new Action() {
                                        @Override
                                        public void run() throws Exception {
                                            e.onNext(user);
                                        }
                                    }));
                                }

                                Completable.merge(completables).doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        e.onComplete();
                                    }
                                }).subscribe();

                            }
                            else {
                                e.onError(ChatError.getError(ChatError.Code.NO_USER_FOUND, "Unable to found user."));
                            }
                        } else {
                            e.onError(ChatError.getError(ChatError.Code.NO_USER_FOUND, "Unable to found user."));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        e.onError(firebaseError.toException());
                    }
                });
            }
        });
    }

    public static String processForQuery(String query){
        return StringUtils.isBlank(query) ? "" : query.replace(" ", "").toLowerCase();
    }

}
