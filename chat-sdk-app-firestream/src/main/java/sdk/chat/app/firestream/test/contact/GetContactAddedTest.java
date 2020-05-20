package sdk.chat.app.firestream.test.contact;

import sdk.guru.common.EventType;
import firestream.chat.namespace.Fire;
import sdk.chat.app.firestream.test.Result;
import sdk.chat.app.firestream.test.Test;
import sdk.chat.app.firestream.test.TestScript;
import io.reactivex.Observable;

public class GetContactAddedTest extends Test {

    public GetContactAddedTest() {
        super("GetContactAdded");
    }

    @Override
    public Observable<Result> run() {
        return Observable.create(emitter -> {
            manage(emitter);
            dm.add(Fire.stream().getContactEvents().currentAndNewEvents().subscribe(userEvent -> {
                if (userEvent.typeIs(EventType.Added)) {
                    if (userEvent.get().equals(TestScript.testUser1())) {
                        complete();
                    } else {
                        failure("Wrong user added");
                    }
                } else {
                    failure("No contact added");
                }
                complete();
            }, this));
        });
    }
}
