package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.hook.Hook;

/**
 * Created by ben on 9/13/17.
 */

public interface HookHandler {

    void addHook(Hook hook, String name);
    void addHook(Hook hook, String... names);

    void removeHook(Hook hook, String name);
    Completable executeHook(String name, Map<String, Object> data);
    Completable executeHook(String name);

}
