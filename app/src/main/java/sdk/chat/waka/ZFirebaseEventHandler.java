package sdk.chat.waka;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sdk.chat.android.live.R;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.FirebaseEventHandler;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.moderation.Permission;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;
import sdk.guru.common.EventType;
import sdk.guru.realtime.RXRealtime;

public class ZFirebaseEventHandler extends FirebaseEventHandler {

    Map<String, DatedThread> threadMap = new HashMap<>();
    List<String> threadEntityIDs = new ArrayList<>();
    List<String> isOn = new ArrayList<>();
    boolean initialLoad = true;
    int i = 0;

    protected List<String> buildLiveThreadList() {
        // Listen to the most
        List<String> ids = new ArrayList<>();

        List<DatedThread> datedThreads = new ArrayList<>();

        for (DatedThread t: threadMap.values()) {
            if (t.timestamp != 0) {
                datedThreads.add(t);
            }
        }
        Collections.sort(datedThreads, (Comparator<DatedThread>) (t1, t2) -> {
            return t1.timestamp > t2.timestamp ? -1 : 1;
        });

        for (int i = 0; i < ZWaka.shared().maxThreadCount - ZWaka.shared().mostRecent; i++) {
            if (datedThreads.size() > i) {
                ids.add(datedThreads.get(i).entityID);
            }
        }

        for (int i = threadEntityIDs.size() - 1; i > 0; i--) {
            if (!ids.contains(threadEntityIDs.get(i))) {
                ids.add(threadEntityIDs.get(i));
            }
            if (ids.size() >= ZWaka.shared().maxThreadCount) {
                break;
            }
        }

        return ids;
    }

    protected void threadsOn(User user) {
        String entityID = user.getEntityID();

        final DatabaseReference threadsRef = FirebasePaths.userThreadsRef(entityID);

        // First get all the threads in one go
        dm.add(new RXRealtime().get(threadsRef).subscribe(dataSnapshotOptional -> {

            Boolean efficiencyMode = false;

            // If the user has no threads or if the number is below the threshold, we just leave it to the standard event listener...
            if (!dataSnapshotOptional.isEmpty()) {
                // For each thread, get the messages updated time

                Iterator<DataSnapshot> it = dataSnapshotOptional.get().getChildren().iterator();
                while (it.hasNext()) {
                    DataSnapshot child = it.next();
                    threadEntityIDs.add(child.getKey());
                }

                // Get a count and check if it is over the threshold
                int size = threadEntityIDs.size();

                if (size > ZWaka.shared().maxThreadCount) {
                    for (String key: threadEntityIDs) {

                        efficiencyMode = true;

                        dm.add(new RXRealtime().on(FirebasePaths.threadRef(key).child("updated").child("messages")).subscribe(documentChange -> {
                            // Add the thread id to the map
                            Long timestamp = documentChange.getSnapshot().getValue(Long.class);
                            if (timestamp == null) {
                                timestamp = 0l;
                            }

                            threadMap.put(key, new DatedThread(key, timestamp));

                            // If we have just built the list of all the threads, then when we have them all, we turn on the
                            // relevant threads
                            if (initialLoad) {
                                if (threadMap.size() == size) {
                                    liveThreadsOn(user);
                                }
                            } else {
                                // Otherwise, we just set it on because it changed...
                                threadOn(user, key, EventType.Added);
                            }

                        }));
                    }
                }
            }

            if (!efficiencyMode) {
                standardThreadListenerOn(user);
            }

        }));

    }

    protected void liveThreadsOn(User user) {
        if (initialLoad) {
            initialLoad = false;

            // Now handle results
            List<String> liveThreadIds = buildLiveThreadList();

            for (String id: liveThreadIds) {
                threadOn(user, id, EventType.Added);
            }

            standardThreadListenerOn(user);
        }
    }

    protected void standardThreadListenerOn(User user) {
        final DatabaseReference threadsRef = FirebasePaths.userThreadsRef(user.getEntityID());

//        long now = new Date().getTime();

        new RXRealtime().childOn(threadsRef).doOnNext(change -> {
            // Check the timestamp... if it exists
            String threadEntityID = change.getSnapshot().getKey();
            if ((change.getType() == EventType.Added && !threadMap.containsKey(threadEntityID)) || change.getType() == EventType.Removed) {
                threadOn(user, threadEntityID, change.getType());
            }
        }).ignoreElements().subscribe(this);
    }

    public void threadOn(User user, String threadEntityID, EventType type) {

//        Logger.warn("Thread A: " + i);

        // Get the last updated timestamp
        final ThreadWrapper thread = FirebaseModule.config().provider.threadWrapper(threadEntityID);

        if (type == EventType.Added && !thread.getModel().typeIs(ThreadType.Public)) {

            if (isOn.contains(threadEntityID)) {
                return;
            }
            isOn.add(threadEntityID);

//            Logger.warn("Thread B: " + i);

            long now = new Date().getTime();
            if (ChatSDK.config().privateChatRoomLifetimeMinutes == 0 || thread.getModel().getCreationDate() == null || (now - thread.getModel().getCreationDate().getTime()) < TimeUnit.MINUTES.toMillis(ChatSDK.config().privateChatRoomLifetimeMinutes)) {

                if (thread.getModel().typeIs(ThreadType.Group)) {
                    String permission = thread.getModel().getPermission(user.getEntityID());
                    if (permission != null && permission.equals(Permission.None)) {
                        ChatSDK.thread().sendLocalSystemMessage(ChatSDK.getString(R.string.you_were_added_to_the_thread), thread.getModel());
                    }
                }

                thread.getModel().addUser(user, false);

//                Logger.warn("Thread C: " + i);

                thread.on().doOnComplete(() -> {
                    ChatSDK.events().source().accept(NetworkEvent.threadAdded(thread.getModel()));
                }).subscribe();

            }

        }
        if (type == EventType.Removed) {
            Logger.debug("Thread removed: " + threadEntityID);

            if (thread.getModel().typeIs(ThreadType.Group)) {
                ChatSDK.thread().sendLocalSystemMessage(ChatSDK.getString(R.string.you_were_removed_from_the_thread), thread.getModel());
            }
            thread.getModel().setPermission(user.getEntityID(), Permission.None, true, false);
            thread.getModel().getUserThreadLink(ChatSDK.currentUser().getId()).setHasLeft(true);

//                ChatSDK.events().source().accept(NetworkEvent.threadRemoved(thread.getModel()));

            thread.off();
        }

        i++;
    }

    protected void threadsOff(User user) {
        super.threadsOff(user);
        threadMap.clear();
        threadEntityIDs.clear();
        isOn.clear();
        initialLoad = true;
    }


}
