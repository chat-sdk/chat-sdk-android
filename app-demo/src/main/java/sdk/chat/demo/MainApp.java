package sdk.chat.demo;

import android.app.Application;

import org.pmw.tinylog.Logger;

import sdk.chat.app.firebase.ChatSDKFirebase;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.receipts.FirebaseReadReceiptsModule;
import sdk.chat.message.audio.AudioMessageModule;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            // Setup Chat SDK
            ChatSDKFirebase.quickStart(this, "pre_1", "your_key_here", true,
                    AudioMessageModule.shared(),
                    FirebaseReadReceiptsModule.shared()
            );

            ChatSDK.events().sourceOnMain().subscribe(event -> {
                Logger.debug(event);
            });

            ChatSDK.events().errorSourceOnMain().subscribe(event -> {
                Logger.debug(event);
                event.printStackTrace();
            });






            //
//            MessageCustomizer.shared().addMessageHandler(new IMessageHandler() {
//                @Override
//                public void onBindMessageHolders(Context context, MessageHolders holders) {
//
//                    holders.setIncomingTextConfig(CustomIncomingTextMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_incoming_text_message)
//                            .setOutcomingTextConfig(CustomOutcomingTextMessageViewHolder.class, sdk.chat.ui.R.layout.view_holder_outcoming_text_message);
//
//                }
//
//                @Override
//                public MessageHolder onNewMessageHolder(Message message) {
//                    return new CustomTextMessageHolder(message);
//                }
//
//                @Override
//                public void onClick(ChatActivity activity, View rootView, Message message) {
//
//                }
//
//                @Override
//                public void onLongClick(ChatActivity activity, View rootView, Message message) {
//
//                }
//
//                @Override
//                public boolean hasContentFor(MessageHolder message, byte type) {
//                    return type == MessageType.Text && message instanceof CustomTextMessageHolder;
//                }
//            });


//            OverrideViewExample.run();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
