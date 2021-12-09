package sdk.chat.core.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.handlers.HookHandler;
import sdk.chat.core.hook.Hook;

/**
 * Created by ben on 9/13/17.
 */

public class BaseHookHandler implements HookHandler {

    protected Map<String, ArrayList<Hook>> hooks = new HashMap<>();

    @Override
    public void addHook(Hook hook, String name) {
        ArrayList<Hook> existingHooks = hooks.get(name);
        if(existingHooks == null) {
            existingHooks = new ArrayList<>();
        }
        if(!existingHooks.contains(hook)) {
            existingHooks.add(hook);
        }
        hooks.put(name, existingHooks);
    }

    @Override
    public void addHook(Hook hook, String... names) {
        for (String name: names) {
            addHook(hook, name);
        }
    }

    @Override
    public void removeHook(Hook hook, String name) {
        ArrayList<Hook> existingHooks = hooks.get(name);
        if(existingHooks != null) {
            existingHooks.remove(hook);
        }
    }

    @Override
    public Completable executeHook(String name, Map<String, Object> data) {
        ArrayList<Hook> existingHooks = hooks.get(name);
        ArrayList<Completable> completables = new ArrayList<>();
        if(existingHooks != null) {
            for(Hook hook : existingHooks) {
                completables.add(hook.executeAsync(data).doOnComplete(() -> {
                    if (hook.removeOnFire) {
                        removeHook(hook, name);
                    }
                }));
            }
        }
        return Completable.merge(completables);
    }

    @Override
    public Completable executeHook(String name) {
        return executeHook(name, null);
    }
}
