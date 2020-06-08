package sdk.chat.app.firestream.test.contact;

import firestream.chat.namespace.Fire;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import sdk.chat.app.firestream.test.Result;
import sdk.chat.app.firestream.test.Test;
import sdk.chat.app.firestream.test.TestScript;
import sdk.guru.common.RX;

public class GetContactRemovedTest extends Test {

    public GetContactRemovedTest() {
        super("GetContactRemoved");
    }

    @Override
    public Observable<Result> run() {
        return Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            manage(emitter);
            dm.add(Fire.stream().getContactEvents().currentAndNewEvents().subscribe(userEvent -> {
                if (userEvent.isRemoved()) {
                    if (userEvent.get().equals(TestScript.testUser1())) {
                        complete();
                    } else {
                        failure("Wrong user removed");
                    }
                } else {
                    failure("No contact removed");
                }
                complete();
            }, this));
        }).subscribeOn(RX.io());
    }

}
