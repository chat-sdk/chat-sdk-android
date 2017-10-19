package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.FirebaseDefines;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.types.ChatError;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebaseSearchHandler implements SearchHandler {

    public Observable<User> usersForIndex(final String index, final String value) {
        return Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(final ObservableEmitter<User> e) throws Exception {

                if (StringUtils.isBlank(value))
                {
                    e.onError(ChatError.getError(ChatError.Code.NULL, "Value is blank"));
                    return;
                }

                final Query query = FirebasePaths.usersRef()
                        .orderByChild(Keys.Meta + '/' + index)
                        .startAt(value)
                        .limitToFirst(FirebaseDefines.NumberOfUserToLoadForIndex);

                final ChildEventListener listener = query.addChildEventListener(new FirebaseEventListener().onChildAdded(new FirebaseEventListener.Change() {
                    @Override
                    public void trigger(DataSnapshot snapshot, String s, boolean hasValue) {
                        if(hasValue) {
                            // Check that the meta/index path contains the necessary value
                            if(snapshot.hasChild(Keys.Meta)) {
                                DataSnapshot meta = snapshot.child(Keys.Meta);
                                if(meta.hasChild(index)) {
                                    String childValue = (String) meta.child(index).getValue();
                                    if(childValue.contains(value)) {
                                        final UserWrapper wrapper = new UserWrapper(snapshot);
                                        if(!wrapper.getModel().equals(NM.currentUser())) {
                                            e.onNext(wrapper.getModel());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));

                e.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        query.removeEventListener(listener);
                    }

                    @Override
                    public boolean isDisposed() {
                        return false;
                    }
                });
            }
        }).subscribeOn(Schedulers.single());
    }

    public static String processForQuery(String query){
        return StringUtils.isBlank(query) ? "" : query.replace(" ", "").toLowerCase();
    }

}
