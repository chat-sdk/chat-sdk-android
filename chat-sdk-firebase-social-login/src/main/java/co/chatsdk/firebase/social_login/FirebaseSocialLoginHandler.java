package co.chatsdk.firebase.social_login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AuthKeys;
import co.chatsdk.firebase.FirebaseAuthenticationHandler;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

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
        return Single.create(new SingleOnSubscribe<AuthCredential>() {
            @Override
            public void subscribe(final SingleEmitter<AuthCredential> e) throws Exception {

                LoginButton button = new LoginButton(activity);
                facebookCallbackManager = CallbackManager.Factory.create();
                button.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        NM.auth().addLoginInfoData(AuthKeys.Token, loginResult.getAccessToken().getToken());
                        NM.auth().addLoginInfoData(AuthKeys.Type, AccountDetails.Type.Facebook.ordinal());

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

            }
        }).flatMapCompletable(new Function<AuthCredential, Completable>() {
            @Override
            public Completable apply(AuthCredential authCredential) throws Exception {
                return signInWithCredential(activity, authCredential);
            }
        });
    }

    @Override
    public Completable loginWithTwitter(final Activity activity) {
        return Single.create(new SingleOnSubscribe<AuthCredential>() {
            @Override
            public void subscribe(final SingleEmitter<AuthCredential> e) throws Exception {

                twitterButton = new TwitterLoginButton(activity);
                twitterButton.setCallback(new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {

                        NM.auth().addLoginInfoData(AuthKeys.Token, result.data.getAuthToken().token);
                        NM.auth().addLoginInfoData(AuthKeys.Type, AccountDetails.Type.Twitter.ordinal());

                        e.onSuccess(TwitterAuthProvider.getCredential(result.data.getAuthToken().token, result.data.getAuthToken().secret));
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        e.onError(exception);
                    }
                });
                twitterButton.callOnClick();

            }
        }).flatMapCompletable(new Function<AuthCredential, Completable>() {
            @Override
            public Completable apply(AuthCredential authCredential) throws Exception {
                return signInWithCredential(activity, authCredential);
            }
        });
    }

    @Override
    public Completable loginWithGoogle(final Activity activity) {
        return Single.create(new SingleOnSubscribe<AuthCredential>() {
            @Override
            public void subscribe(final SingleEmitter<AuthCredential> e) throws Exception {

                googleClient = new GoogleApiClient.Builder(activity)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
                activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);

                googleSignInCompleteListener = new GoogleSignInCompleteListener() {
                    @Override
                    public void complete(GoogleSignInResult result) {
                        if(result.isSuccess()) {
                            AuthCredential credential = GoogleAuthProvider.getCredential(result.getSignInAccount().getIdToken(), null);
                            e.onSuccess(credential);
                        }
                        else {
                            e.onError(new Exception(result.getStatus().getStatusMessage()));
                        }
                    }
                };

            }
        }).flatMapCompletable(new Function<AuthCredential, Completable>() {
            @Override
            public Completable apply(AuthCredential authCredential) throws Exception {
                return signInWithCredential(activity, authCredential);
            }
        });
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
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful() && task.isComplete()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    FirebaseAuthenticationHandler handler = (FirebaseAuthenticationHandler) NM.auth();

                                    handler.authenticateWithUser(user).doOnError(new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            throwable.printStackTrace();
                                            e.onError(throwable);
                                        }
                                    }).subscribe(new Action() {
                                        @Override
                                        public void run() throws Exception {
                                            e.onComplete();
                                        }
                                    });
                                }
                                else {
//                                    Toast.makeText(context, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
                                    e.onError(task.getException());
                                }
                            }
                        });
            }
        });
    }



}
