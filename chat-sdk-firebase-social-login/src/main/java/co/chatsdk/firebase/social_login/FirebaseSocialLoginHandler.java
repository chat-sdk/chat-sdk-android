package co.chatsdk.firebase.social_login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import co.chatsdk.core.handlers.SocialLoginHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.firebase.FirebaseAuthenticationHandler;
import co.chatsdk.firebase.FirebaseCoreHandler;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by ben on 9/4/17.
 */

public class FirebaseSocialLoginHandler implements SocialLoginHandler {

    // Facebook
    private CallbackManager facebookCallbackManager;

    // Google
    private GoogleSignInOptions gso;
    private GoogleApiClient googleClient;
    private GoogleSignInCompleteListener googleSignInCompleteListener;

    // Twitter
    TwitterLoginButton twitterButton;

    private static int RC_GOOGLE_SIGN_IN = 200;

    public FirebaseSocialLoginHandler (Context context) {

        if(accountTypeEnabled(AccountDetails.Type.Google)) {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(ChatSDK.config().googleWebClientKey)
                    .requestEmail()
                    .build();
        }
        if(accountTypeEnabled(AccountDetails.Type.Twitter)) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(ChatSDK.config().twitterKey, ChatSDK.config().twitterSecret);
            TwitterConfig config = new TwitterConfig.Builder(context).twitterAuthConfig(authConfig).build();
            Twitter.initialize(config);
        }
    }

    interface GoogleSignInCompleteListener {
        void complete (GoogleSignInResult result);
    }

    @Override
    public Completable loginWithFacebook(final Activity activity) {
        return Single.create((SingleOnSubscribe<AuthCredential>) e -> {

            LoginButton button = new LoginButton(activity);
            facebookCallbackManager = CallbackManager.Factory.create();
            button.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    e.onSuccess(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()));
                }

                @Override
                public void onCancel() {
                    e.onError(null);
                }

                @Override
                public void onError(FacebookException error) {
                    e.onError(error);
                }
            });

            button.callOnClick();

        }).flatMapCompletable(authCredential -> signInWithCredential(activity, authCredential));
    }

    @Override
    public Completable loginWithTwitter(final Activity activity) {
        return Single.create((SingleOnSubscribe<AuthCredential>) e -> {

            twitterButton = new TwitterLoginButton(activity);
            twitterButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    e.onSuccess(TwitterAuthProvider.getCredential(result.data.getAuthToken().token, result.data.getAuthToken().secret));
                }

                @Override
                public void failure(TwitterException exception) {
                    e.onError(exception);
                }
            });
            twitterButton.callOnClick();

        }).flatMapCompletable(authCredential -> signInWithCredential(activity, authCredential));
    }

    @Override
    public Completable loginWithGoogle(final Activity activity) {
        return Single.create((SingleOnSubscribe<AuthCredential>) e -> {

            googleClient = new GoogleApiClient.Builder(activity)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
            activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);

            googleSignInCompleteListener = result -> {
                if(result.isSuccess()) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(result.getSignInAccount().getIdToken(), null);
                    e.onSuccess(credential);
                }
                else {
                    e.onError(new Exception(result.getStatus().toString()));
                }
            };

        }).flatMapCompletable(authCredential -> signInWithCredential(activity, authCredential));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(facebookCallbackManager != null) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(googleSignInCompleteListener != null) {
                googleSignInCompleteListener.complete(result);
            }
        }

        if(twitterButton != null) {
            twitterButton.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public boolean accountTypeEnabled(AccountDetails.Type type) {
        switch (type) {
            case Facebook:
                return ChatSDK.config().facebookLoginEnabled();
            case Twitter:
                return ChatSDK.config().twitterLoginEnabled();
            case Google:
                return ChatSDK.config().googleLoginEnabled();
            default:
                return false;
        }
    }

    @Override
    public void logout() {
        LoginManager.getInstance().logOut();
    }

    public Completable signInWithCredential (final Activity activity, final AuthCredential credential) {
        return Completable.create(e -> FirebaseCoreHandler.auth().signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful() && task.isComplete()) {
                        FirebaseUser user = FirebaseCoreHandler.auth().getCurrentUser();

                        FirebaseAuthenticationHandler handler = (FirebaseAuthenticationHandler) ChatSDK.auth();

                        handler.authenticateWithUser(user).subscribe(e::onComplete, e::onError);
                    }
                    else {
                        e.onError(task.getException());
                    }
                }));
    }



}
