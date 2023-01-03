package firestream.chat.firestore;

import firestream.chat.firebase.service.FirebaseService;

public class FirestoreService extends FirebaseService {

    public FirestoreService() {
        core = new FirestoreCoreHandler();
        chat = new FirestoreChatHandler();
    }
}
