package sdk.chat.dcom;

import sdk.chat.core.dao.Thread;
import sdk.chat.firebase.adapter.utils.FirebaseProvider;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;

public class DComFirebaseProvider extends FirebaseProvider {

    public ThreadWrapper threadWrapper(String value) {
        return new DComThreadWrapper(value);
    }
    public ThreadWrapper threadWrapper(Thread value) {
        return new DComThreadWrapper(value);
    }

}
