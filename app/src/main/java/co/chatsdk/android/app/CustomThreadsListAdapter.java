package co.chatsdk.android.app;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.threads.ThreadsListAdapter;

public class CustomThreadsListAdapter extends ThreadsListAdapter {

    public CustomThreadsListAdapter(Context context) {
        super(context);
    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
    public void filterThreads(String filter) {
        List<Thread> filteredThreads = new ArrayList<>();
        List<Thread> newThreads = NM.thread().getThreads(ThreadType.Public);
        for(Thread t : newThreads) {
            if (t.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredThreads.add(t);
            }
        }
        clearData();
        updateThreads(filteredThreads);

//        if (filter != null && !filter.equals("")) {
//            newThreads.removeIf(new Predicate<Thread>() {
//                @Override
//                public boolean test(Thread t) {
//                    return !t.getName().toLowerCase().contains(filter.toLowerCase());
//                }
//            });


    }

}
