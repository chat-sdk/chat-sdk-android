package sdk.chat.demo;

import android.app.Application;

import sdk.chat.ui.module.UIModule;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


//        ChatSDK.shared().setOnActivateListener(() -> {
//            ChatSDK.hook().addHook(Hook.sync(data -> ChatSDK.core().getUserForEntityID("4hekpBRhB6gO03EvUwcl53W9xX83").subscribe(user -> {
//                Logger.debug("");
//            })), HookEvent.DidAuthenticate);
//        });

    }


}
