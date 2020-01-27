package firestream.chat.test.contact;

import firestream.chat.events.EventType;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.test.TestScript;
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
