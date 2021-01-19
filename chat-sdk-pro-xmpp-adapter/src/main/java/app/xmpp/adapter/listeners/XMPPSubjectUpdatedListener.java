package app.xmpp.adapter.listeners;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jxmpp.jid.EntityFullJid;

import java.lang.ref.WeakReference;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import io.reactivex.disposables.Disposable;


public class XMPPSubjectUpdatedListener implements SubjectUpdatedListener, Disposable {

    private WeakReference<MultiUserChat> chat;
    boolean disposed = false;

    public XMPPSubjectUpdatedListener(MultiUserChat chat) {
        this.chat = new WeakReference<>(chat);
        addListeners();
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
        disposed = true;
    }


    public void removeListeners() {
        MultiUserChat chat = this.chat.get();
        if (chat != null) {
            chat.removeSubjectUpdatedListener(this);
        }
    }

    public void addListeners() {
        MultiUserChat chat = this.chat.get();
        if (chat != null) {
            removeListeners();
            chat.addSubjectUpdatedListener(this);
        }
    }


    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
