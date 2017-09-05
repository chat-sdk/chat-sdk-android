package co.chatsdk.core.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import co.chatsdk.core.types.AccountDetails;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface SocialLoginHandler {

    Completable loginWithFacebook(Activity activity);
    Completable loginWithTwitter(Activity activity);
    Completable loginWithGoogle(Activity activity);

    // This should be called by the activity
    void onActivityResult(int requestCode, int resultCode, Intent data);

    boolean accountTypeEnabled (AccountDetails.Type type);
    void logout();

}
