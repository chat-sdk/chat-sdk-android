package firestream.chat.realtime;

import firestream.chat.firebase.service.FirebaseService;

public class RealtimeService extends FirebaseService {

    public RealtimeService() {
        core = new RealtimeCoreHandler();
        chat = new RealtimeChatHandler();
    }

}
