package co.chatsdk.android.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.braunster.chatsdk.network.FirebaseSocialLoginModule;

import co.chatsdk.core.ChatSDK;
import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NM;
import co.chatsdk.core.dao.DaoCore;

import co.chatsdk.core.utils.AppContext;

import co.chatsdk.firebase.FirebaseModule;
import co.chatsdk.firebase.filestorage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushHandler;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.xmpp.XMPPModule;
import timber.log.Timber;
import co.chatsdk.ui.helpers.UIHelper;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        Context context = getApplicationContext();

        // Logging tool.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(Timber.asTree());
        }


        AppContext.shared().setContext(getApplicationContext());

        ChatSDK.shared().setContext(context);

        XMPPModule.activate();
//        FirebaseModule.activate();

        FirebaseFileStorageModule.activate();
        FirebasePushModule.activate(new FirebasePushHandler.TokenPusher() {
            @Override
            public void pushToken() {
                NM.core().pushUser();
            }
        });
        //FirebaseSocialLoginModule.activate(this);
    }
}
