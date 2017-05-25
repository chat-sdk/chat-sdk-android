package co.chatsdk.core;

import java.util.List;

import co.chatsdk.core.dao.core.BUser;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class BaseContactHandler {

    /**
     * @return the current user contacts list.
     **/
    public List<BUser> getContacts() {
        return NetworkManager.shared().a.core.currentUserModel().getContacts();
    }

}
