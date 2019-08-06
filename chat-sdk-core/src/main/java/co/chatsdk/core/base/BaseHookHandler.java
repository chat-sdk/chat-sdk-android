package co.chatsdk.core.base;

import java.util.ArrayList;
import java.util.HashMap;

import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.hook.Hook;
import io.reactivex.Completable;

/**
 * Created by ben on 9/13/17.
 */

public class BaseHookHandler implements HookHandler {

    protected HashMap<String, ArrayList<Hook>> hooks = new HashMap<>();

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
    public void removeHook(Hook hook, String name) {
        ArrayList<Hook> existingHooks = hooks.get(name);
        if(existingHooks != null) {
            existingHooks.remove(hook);
        }
    }

    @Override
    public Completable executeHook(String name, HashMap<String, Object> data) {
        ArrayList<Hook> existingHooks = hooks.get(name);
        ArrayList<Completable> completables = new ArrayList<>();
        if(existingHooks != null) {
            for(Hook hook : existingHooks) {
                completables.add(hook.execute(data));
            }
        }
        return Completable.merge(completables);
    }

    @Override
    public Completable executeHook(String name) {
        return executeHook(name, null);
    }
}
