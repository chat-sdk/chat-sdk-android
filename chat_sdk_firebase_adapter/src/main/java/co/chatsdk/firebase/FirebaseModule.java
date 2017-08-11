package co.chatsdk.firebase;

import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.ui.BaseInterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class FirebaseModule implements Module {
    @Override
    public void activate() {
        NetworkManager.shared().a = new FirebaseNetworkAdapter();
        InterfaceManager.shared().a = new BaseInterfaceAdapter();
    }
}
