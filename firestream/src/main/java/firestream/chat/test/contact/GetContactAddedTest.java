package firestream.chat.test.contact;

import firestream.chat.namespace.Fire;
import io.reactivex.Observable;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.test.TestScript;

public class GetContactAddedTest extends Test {

    public GetContactAddedTest() {
        super("GetContactAdded");
    }

    @Override
    public Observable<Result> run() {
        return Observable.create(emitter -> {
            manage(emitter);
            dm.add(Fire.stream().getContactEvents().currentAndNewEvents().subscribe(userEvent -> {
                if (userEvent.isAdded()) {
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
