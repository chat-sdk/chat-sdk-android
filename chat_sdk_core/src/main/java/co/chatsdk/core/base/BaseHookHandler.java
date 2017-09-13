package co.chatsdk.core.base;

import java.util.ArrayList;
import java.util.HashMap;

import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.hook.Hook;

/**
 * Created by ben on 9/13/17.
 */

public class BaseHookHandler implements HookHandler {

    public static String UserAuthFinished = "UserAuthFinished";
    public static String UserAuthFinished_User = "UserAuthFinished_User";

    public static String UserOn = "UserOn";
    public static String UserOn_User = "UserOn_User";

    public static String MessageReceived = "MessageReceived";
    public static String MessageReceived_Message = "MessageReceived_Message";

    public static String Logout = "Logout";
    public static String Logout_User = "Logout_User";

    HashMap<String, ArrayList> hooks = new HashMap<>();

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
    public void executeHook(String name, HashMap<String, Object> data) {
        ArrayList<Hook> existingHooks = hooks.get(name);
        if(existingHooks != null) {
            for(Hook hook : existingHooks) {
                hook.execute(data);
            }
        }
    }
}
