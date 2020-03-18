package co.chatsdk.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.HashMap;

import co.chatsdk.core.base.AbstractSearchHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ChatError;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebaseSearchHandler extends AbstractSearchHandler {

    @Override
    public Observable<User> usersForIndex(String value, int limit) {
        return usersForIndexes(value, limit, ChatSDK.config().searchIndexes);
    }

    @Override
    public Observable<User> usersForIndex(final String finalValue, final int limit, final String index) {
        return Observable.create((ObservableOnSubscribe<User>) e -> {

            if (finalValue.replace(" ", "").isEmpty())
            {
                e.onError(new Exception(ChatSDK.shared().getString(R.string.search_field_empty)));
                return;
            }

            String value = finalValue;

            if (index.equals(Keys.NameLowercase)) {
                value = value.toLowerCase();
            }

            final Query query = FirebasePaths.usersRef()
                    .orderByChild(Keys.Meta + '/' + index)
                    .startAt(value)
                    .limitToFirst(limit);

            query.keepSynced(true);

            query.addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if (hasValue) {
                    Object valueObject = snapshot.getValue();
                    if (valueObject instanceof HashMap) {
                        for (Object key : ((HashMap) valueObject).keySet()) {
                            if (key instanceof String) {
                                DataSnapshot userSnapshot = snapshot.child((String) key);

                                if (userSnapshot.hasChild(Keys.Meta)) {
                                    DataSnapshot meta = userSnapshot.child(Keys.Meta);
                                    if (meta.hasChild(index)) {
                                        String childValue = (String) meta.child(index).getValue();
                                        String name = (String) meta.child(Keys.Name).getValue();
                                        if (childValue != null && childValue.toLowerCase().indexOf(finalValue.toLowerCase()) == 0 && name != null && !name.isEmpty()) {
                                            final UserWrapper wrapper = new UserWrapper(userSnapshot);
                                            if (!wrapper.getModel().equals(ChatSDK.currentUser()) && !ChatSDK.contact().exists(wrapper.getModel())) {
                                                e.onNext(wrapper.getModel());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                e.onComplete();
            }).onCancelled(error -> {
                if(!e.isDisposed()) {
                    e.onError(new Throwable(error.getMessage()));
                }
            }));

        }).subscribeOn(Schedulers.io());
    }

}
