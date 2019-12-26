package sdk.chat.micro.firebase.firestore;

import sdk.chat.micro.firebase.service.FirebaseService;

public class FirestoreService extends FirebaseService {

    public FirestoreService() {
        core = new FirestoreCoreHandler();
        chat = new FirestoreChatHandler();
    }
}
