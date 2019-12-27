package firefly.sdk.chat.firebase.firestore;

import firefly.sdk.chat.firebase.service.FirebaseService;

public class FirestoreService extends FirebaseService {

    public FirestoreService() {
        core = new FirestoreCoreHandler();
        chat = new FirestoreChatHandler();
    }
}
