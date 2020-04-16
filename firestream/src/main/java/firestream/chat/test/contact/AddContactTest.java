package firestream.chat.test.contact;

import java.util.List;

import firestream.chat.chat.User;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.test.TestScript;
import firestream.chat.types.ContactType;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import sdk.guru.common.RX;

public class AddContactTest extends Test {

    public AddContactTest() {
        super("AddContact");
    }

    @Override
    public Observable<Result> run() {
        return Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            manage(emitter);
            User user = TestScript.testUser1();
            dm.add(Fire.stream().addContact(user, ContactType.contact()).subscribe(() -> {
                // Check that it exists in the contact list
                List<User> contacts = Fire.stream().getContacts();

                if (contacts.size() != 1) {
                    failure("Contact size must be 1");
                } else if (!contacts.get(0).equals(user)) {
                    failure("Correct user not added to contacts");
                } else {
                    complete();
                }
            }, this));
        }).subscribeOn(RX.io());
    }

}
