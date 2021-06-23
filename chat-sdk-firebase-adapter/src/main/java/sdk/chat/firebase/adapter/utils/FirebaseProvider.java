package sdk.chat.firebase.adapter.utils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.firebase.adapter.wrappers.MessageWrapper;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;

public class FirebaseProvider {

    public UserWrapper userWrapper(FirebaseUser value) {
        return new UserWrapper(value);
    }

    public UserWrapper userWrapper(User value) {
        return new UserWrapper(value);
    }

    public UserWrapper userWrapper(DataSnapshot value) {
        return new UserWrapper(value);
    }

    public UserWrapper userWrapper(String value) {
        return new UserWrapper(value);
    }

    public ThreadWrapper threadWrapper(String value) {
        return new ThreadWrapper(value);
    }

    public ThreadWrapper threadWrapper(Thread value) {
        return new ThreadWrapper(value);
    }

    public MessageWrapper messageWrapper(Message value) {
        return new MessageWrapper(value);
    }

    public MessageWrapper messageWrapper(DataSnapshot value) {
        return new MessageWrapper(value);
    }
    public MessageWrapper messageWrapper(String value) {
        return new MessageWrapper(value);
    }

}
