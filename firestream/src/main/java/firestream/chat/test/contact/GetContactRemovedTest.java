package firestream.chat.test.contact;

import firestream.chat.events.EventType;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.test.TestScript;
import io.reactivex.Observable;

public class GetContactRemovedTest extends Test {

    public GetContactRemovedTest() {
        super("GetContactRemoved");
    }

    @Override
    public Observable<Result> run() {
        return Observable.create(emitter -> {
            manage(emitter);
            dm.add(Fire.Stream.getContactEvents().currentAndNewEvents().subscribe(userEvent -> {
                if (userEvent.type == EventType.Removed) {
                    if (userEvent.user.equals(TestScript.testUser1())) {
                        complete();
                    } else {
                        failure("Wrong user removed");
                    }
                } else {
                    failure("No contact removed");
                }
                complete();
            }, this));
        });
    }

}
