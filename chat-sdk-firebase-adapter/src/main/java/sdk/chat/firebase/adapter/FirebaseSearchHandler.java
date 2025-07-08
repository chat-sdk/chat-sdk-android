package sdk.chat.firebase.adapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import sdk.chat.core.base.AbstractSearchHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;
import sdk.guru.common.RX;
import sdk.guru.realtime.RealtimeEventListener;

import sdk.chat.core.R;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebaseSearchHandler extends AbstractSearchHandler {

    @Override
    public Observable<User> usersForIndex(String value, int limit) {
        return usersForIndexes(value, limit, FirebaseModule.config().searchIndexes);
    }

    @Override
    public Observable<User> usersForIndex(final String finalValue, final int limit, final String index) {
        return Observable.create((ObservableOnSubscribe<User>) e -> {

            if (finalValue.replace(" ", "").isEmpty())
            {
                e.onError(ChatSDK.getException(R.string.search_field_empty));
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

            query.addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
                if (hasValue) {
                    Object valueObject = snapshot.getValue();
                    if (valueObject instanceof HashMap) {
                        for (Object key : ((Map) valueObject).keySet()) {
                            if (key instanceof String) {
                                DataSnapshot userSnapshot = snapshot.child((String) key);

                                if (userSnapshot.hasChild(Keys.Meta)) {
                                    DataSnapshot meta = userSnapshot.child(Keys.Meta);
                                    if (meta.hasChild(index)) {
                                        String childValue = meta.child(index).getValue(String.class);
                                        String name = meta.child(Keys.Name).getValue(String.class);
                                        if (childValue != null && childValue.toLowerCase().indexOf(finalValue.toLowerCase()) == 0 && name != null && !name.isEmpty()) {
                                            final UserWrapper wrapper = FirebaseModule.config().provider.userWrapper(userSnapshot);
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

        }).subscribeOn(RX.io());
    }

}
