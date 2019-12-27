package firefly.sdk.chat.firebase.realtime;

import firefly.sdk.chat.firebase.service.FirebaseService;

public class RealtimeService extends FirebaseService {

    public RealtimeService() {
        core = new RealtimeCoreHandler();
        chat = new RealtimeChatHandler();
    }

}
