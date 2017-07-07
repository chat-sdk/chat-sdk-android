package co.chatsdk.xmpp.handlers;

import com.google.android.gms.maps.model.LatLng;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;

import java.util.List;

import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.types.MessageUploadResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPThreadHandler extends AbstractThreadHandler {

    @Override
    public Single<BThread> createThread(String name, List<BUser> users) {
        return null;
    }

    @Override
    public Single<BThread> createThread(List<BUser> users) {
        return null;
    }

    @Override
    public Single<BThread> createThread(String name, BUser... users) {
        return null;
    }

    @Override
    public Completable removeUsersFromThread(BThread thread, List<BUser> users) {
        return null;
    }

    @Override
    public Completable removeUsersFromThread(BThread thread, BUser... users) {
        return null;
    }

    @Override
    public Completable addUsersToThread(BThread thread, List<BUser> users) {
        return null;
    }

    @Override
    public Completable addUsersToThread(BThread thread, BUser... users) {
        return null;
    }

    @Override
    public Single<List<BMessage>> loadMoreMessagesForThread(BMessage fromMessage, BThread thread) {
        return null;
    }

    @Override
    public Completable deleteThread(BThread thread) {
        return null;
    }

    @Override
    public Completable leaveThread(BThread thread) {
        return null;
    }

    @Override
    public Completable joinThread(BThread thread) {
        return null;
    }

    @Override
    public Completable sendMessage(BMessage message) {
//        xmppServiceConnection.getConnection().observeOn(Schedulers.single()).doOnNext(new Consumer<AbstractXMPPConnection>() {
//            @Override
//            public void accept(AbstractXMPPConnection abstractXMPPConnection) throws Exception {
//                ChatManager chatManager = ChatManager.getInstanceFor(abstractXMPPConnection);
//                Chat chat = chatManager.getThreadChat(message.getThread().getEntityID());
//                chat.sendMessage(convertToXmppMessage(message));
//            }
//        }).doOnComplete(new Action() {
//            @Override
//            public void run() throws Exception {
//                deferred.resolve(message);
//            }
//        });
        return null;
    }

    @Override
    public void sendLocalSystemMessage(String text, BThread thread) {

    }

    @Override
    public void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, BThread thread) {

    }

    @Override
    public Completable pushThread(BThread thread) {
        return null;
    }

    private Message convertToXmppMessage(BMessage message){
        Message xmppMessage = new Message();
        xmppMessage.setBody(message.getText());
        return xmppMessage;
    }

}
