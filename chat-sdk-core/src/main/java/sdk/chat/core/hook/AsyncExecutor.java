package sdk.chat.core.hook;

import java.util.HashMap;

import io.reactivex.Completable;

public interface AsyncExecutor {
    Completable executeAsync (HashMap<String, Object> data);
}
