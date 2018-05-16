package co.chatsdk.android.app;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.threads.ThreadsListAdapter;

public class CustomThreadsListAdapter extends ThreadsListAdapter {

    public CustomThreadsListAdapter(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void filterThreads(String filter) {
        List<Thread> newThreads = NM.thread().getThreads(ThreadType.Public);
        if (filter != null && !filter.equals("")) {
            newThreads.removeIf(t -> !t.getName().toLowerCase().contains(filter.toLowerCase()));
        }
        clearData();
        updateThreads(newThreads);
    }

}
