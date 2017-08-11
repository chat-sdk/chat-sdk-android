package co.chatsdk.ui.threads;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.Strings;

/**
 * Created by benjaminsmiley-andrews on 11/07/2017.
 */

public class ThreadListItem {

    public static final int CELL_TYPE_THREAD = 0;

    private Thread thread;

    public ThreadListItem (Thread thread) {
        this.thread = thread;
    }

    public String getEntityID () {
        return thread.getEntityID();
    }

    public String getName () {
        return Strings.nameForThread(thread);
    }

    public boolean getIsPrivate () {
        return thread.typeIs(ThreadType.Private);
    }

    public Integer getType () {
        return CELL_TYPE_THREAD;
    }

    public String getLastMessageDateAsString () {
        if(getLastMessageDate() != null) {
            return Strings.dateTime(getLastMessageDate());
        }
        return null;
    }

    public String getLastMessageText () {
        String messageText = Strings.t(R.string.not_no_messages);
        Message lastMessage = thread.lastMessage();
        if(lastMessage != null) {
            messageText = Strings.payloadAsString(lastMessage);
        }
        return messageText;
    }

    public int getUserCount() {
        return thread.getUsers().size();
    }

    public int getUnreadMessagesCount() {
        return thread.getUnreadMessagesAmount();
    }

    public long getId () {
        return thread.getId();
    }

    public Date getLastMessageDate () {
        return thread.getLastMessageAddedDate();
    }

    public Thread getThread () {
        return thread;
    }

    public static boolean compare(ThreadListItem newThread , ThreadListItem oldThread){

        if (newThread.getLastMessageDate() == null || oldThread.getLastMessageDate() == null) {
            return true;
        }

        if (newThread.getLastMessageDate().getTime() > oldThread.getLastMessageDate().getTime()) {
            return true;
        }

        if (!newThread.getName().equals(oldThread.getName())) {
            return true;
        }

        if (newThread.getUserCount() != oldThread.getUserCount()) {
            return true;
        }

        if (StringUtils.isEmpty(newThread.thread.getImageURL()) && StringUtils.isEmpty(oldThread.thread.getImageURL())) {
            return false;
        }

        return !newThread.thread.getImageURL().equals(oldThread.thread.getImageURL());
    }

}
