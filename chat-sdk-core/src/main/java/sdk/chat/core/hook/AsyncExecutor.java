package sdk.chat.core.hook;

import java.util.Map;

import io.reactivex.Completable;

public interface AsyncExecutor {
    Completable executeAsync(Map<String, Object> data);
}
