package sdk.chat.dcom;

import sdk.chat.core.dao.ThreadX;
import sdk.chat.firebase.adapter.utils.FirebaseProvider;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;

public class DComFirebaseProvider extends FirebaseProvider {

    public ThreadWrapper threadWrapper(String value) {
        return new DComThreadWrapper(value);
    }
    public ThreadWrapper threadWrapper(ThreadX value) {
        return new DComThreadWrapper(value);
    }

}
