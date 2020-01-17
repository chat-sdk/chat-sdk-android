package co.chatsdk.core.examples;

import java.util.HashMap;

import co.chatsdk.core.hook.Executor;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.utils.DisposableMap;
import io.reactivex.functions.Consumer;

public class AuthExamples extends BaseExample {

    public AuthExamples() {

        // Login
        dm.add(ChatSDK.auth().authenticate(AccountDetails.username("username", "password")).subscribe(() -> {
            // Handle completion
        }, this));

        // Sign up
        dm.add(ChatSDK.auth().authenticate(AccountDetails.signUp("username", "password")).subscribe(() -> {
            // Handle completion
        }, this));

        // Check if the user is authenticated
        ChatSDK.auth().isAuthenticated();

        // Check if the user is authenticated this session
        ChatSDK.auth().isAuthenticatedThisSession();

        // Logout
        dm.add(ChatSDK.auth().logout().subscribe(() -> {
            // Handle completion
        }, this));

        // Events - there are more events available - look at the HookEvent object
        ChatSDK.hook().addHook(Hook.sync(data -> {

        }), HookEvent.DidAuthenticate);

    }

}
