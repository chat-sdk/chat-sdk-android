package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jxmpp.jid.EntityFullJid;

import java.lang.ref.WeakReference;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.disposables.Disposable;


public class XMPPSubjectUpdatedListener implements SubjectUpdatedListener, Disposable {

    private WeakReference<MultiUserChat> chat;
    boolean isDisposed = false;

    public XMPPSubjectUpdatedListener(MultiUserChat chat) {
        this.chat = new WeakReference<>(chat);
    }

    @Override
    public void subjectUpdated(String subject, EntityFullJid from) {
        // Get the thread
        if (from != null) {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(from.asBareJid().toString());
            if (thread != null) {
                thread.setMetaValue(Keys.Subject, subject);
            }
        }
    }

    @Override
    public void dispose() {
        chat.get().removeSubjectUpdatedListener(this);
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
