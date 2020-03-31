package co.chatsdk.xmpp.handlers;

import co.chatsdk.core.base.AbstractEventHandler;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPEventHandler extends AbstractEventHandler {

    @Override
    public void impl_currentUserOn(String userEntityID) {}

    @Override
    public void impl_currentUserOff(String userEntityID) {}

}
