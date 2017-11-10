package co.chatsdk.core.handlers;

import java.util.HashMap;

import co.chatsdk.core.hook.Hook;

/**
 * Created by ben on 9/13/17.
 */

public interface HookHandler {

    void addHook (Hook hook, String name);
    void removeHook (Hook hook, String name);
    void executeHook (String name, HashMap<String, Object> data);

}
