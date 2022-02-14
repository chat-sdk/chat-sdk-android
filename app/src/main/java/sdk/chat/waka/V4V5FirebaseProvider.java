package sdk.chat.waka;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.firebase.adapter.utils.FirebaseProvider;
import sdk.chat.firebase.adapter.wrappers.MessageWrapper;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;

public class V4V5FirebaseProvider extends FirebaseProvider {

    public UserWrapper userWrapper(FirebaseUser value) {
        return new V4V5UserWrapper(value);
    }

    public UserWrapper userWrapper(User value) {
        return new V4V5UserWrapper(value);
    }

    public UserWrapper userWrapper(DataSnapshot value) {
        return new V4V5UserWrapper(value);
    }

    public UserWrapper userWrapper(String value) {
        return new V4V5UserWrapper(value);
    }

    public ThreadWrapper threadWrapper(String value) {
        return new V4V5ThreadWrapper(value);
    }

    public ThreadWrapper threadWrapper(Thread value) {
        return new V4V5ThreadWrapper(value);
    }

    public MessageWrapper messageWrapper(Message value) {
        return new V4V5MessageWrapper(value);
    }

    public MessageWrapper messageWrapper(DataSnapshot value) {
        return new V4V5MessageWrapper(value);
    }

    public MessageWrapper messageWrapper(String value) {
        return new V4V5MessageWrapper(value);
    }

}
