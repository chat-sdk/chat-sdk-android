package sdk.chat.examples;

import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;

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
