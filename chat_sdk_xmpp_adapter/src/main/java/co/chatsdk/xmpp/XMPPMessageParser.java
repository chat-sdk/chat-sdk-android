package co.chatsdk.xmpp;

import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.joda.time.DateTime;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.Date;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.xmpp.defines.XMPPDefines;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 11/07/2017.
 */

public class XMPPMessageParser {

    public static Single<Message> parse (final org.jivesoftware.smack.packet.Message xmppMessage) {
        return parse(xmppMessage, xmppMessage.getFrom().asBareJid().toString());
    }

    public static Single<Message> parse (final org.jivesoftware.smack.packet.Message xmppMessage, final String senderStringJID) {
        return Single.create(new SingleOnSubscribe<Message>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Message> e) throws Exception {

                // If the message is empty do nothing
                if(xmppMessage.getBody() == null) {
                    e.onSuccess(null);
                    return;
                }

                Jid threadJID = xmppMessage.getFrom();
                Jid senderJID = JidCreate.bareFrom(senderStringJID);

                // Don't handle the message if we sent it!
                if(senderJID.asBareJid().toString().equals(NM.currentUser().getEntityID())) {
                    e.onSuccess(null);
                    return;
                }

//                if(xmppMessage.getType() == org.jivesoftware.smack.packet.Message.Type.groupchat) {
//
//                }

                // Set the thread
                Thread thread = StorageManager.shared().fetchThreadWithEntityID(threadJID.asBareJid().toString());
                if(thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);
                    thread.setEntityID(senderJID.asBareJid().toString());
                    thread.setType(ThreadType.Private1to1);
                    thread.setCreationDate(new Date());
                    thread.setCreatorEntityId(senderJID.asBareJid().toString());

                    // Add the sender
                    User sender = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, senderJID.asBareJid().toString());
                    thread.addUsers(sender, NM.currentUser());
                }

                // Check to see if the message already exists...
                if(thread.containsMessageWithID(xmppMessage.getStanzaId())) {
                    e.onSuccess(null);
                    return;
                }

                final Message message = DaoCore.getEntityForClass(Message.class);
                DaoCore.createEntity(message);

                final Thread finalThread = thread;

                message.setEntityID(xmppMessage.getStanzaId());

                message.setTextString(xmppMessage.getBody());

                DelayInformation delay = xmppMessage.getExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE);
                if(delay != null) {
                    message.setDate(new DateTime(delay.getStamp()));
                }
                else {
                    message.setDate(new DateTime());
                }

                message.setDelivered(Message.Delivered.Yes);

                // Does the message have an extension?
                StandardExtensionElement element = xmppMessage.getExtension(XMPPDefines.Extras, XMPPDefines.MessageNamespace);
                if(element != null) {
                    String type = element.getFirstElement(XMPPDefines.Type).getText();
                    message.setType(Integer.parseInt(type));
                }
                else {
                    message.setType(Message.Type.TEXT);
                }

                //thread.update();
                // Is this a new user?
                User user = StorageManager.shared().fetchUserWithEntityID(senderStringJID);
                if(user == null) {
                    XMPPManager.shared().userManager.updateUserFromVCard(senderJID).subscribe(new Consumer<User>() {
                        @Override
                        public void accept(@NonNull User user) throws Exception {
                            message.setSender(user);
                            message.update();
                            finalThread.addMessage(message);

                            e.onSuccess(message);
                        }
                    });
                }
                else {
                    message.setSender(user);
                    message.update();
                    finalThread.addMessage(message);
                    e.onSuccess(message);
                }

            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }
}
