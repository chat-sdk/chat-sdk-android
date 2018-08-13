package co.chatsdk.firebase.push;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NetworkManager;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule  {

    public static void activateForFirebase () {
        FirebasePushModule.activate(() -> ChatSDK.core().pushUser().subscribe(new CrashReportingCompletableObserver()));
    }

    public static void activateForXMPP () {
        FirebasePushModule.activate(() -> ChatSDK.core().pushUser().doOnComplete(() -> ChatSDK.core().goOnline()).subscribe(new CrashReportingCompletableObserver()));
    }


    public static void activate (FirebasePushHandler.TokenPusher pusher) {
        NetworkManager.shared().a.push = new FirebasePushHandler(pusher);
    }

}