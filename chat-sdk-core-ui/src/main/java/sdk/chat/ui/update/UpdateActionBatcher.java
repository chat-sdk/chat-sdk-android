package sdk.chat.ui.update;

import com.jakewharton.rxrelay2.PublishRelay;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import sdk.guru.common.DisposableMap;
import sdk.chat.core.dao.Thread;
import sdk.guru.common.RX;

public class UpdateActionBatcher {

    DisposableMap dm = new DisposableMap();

    protected Map<Thread, Map<ThreadUpdateAction.Type, ThreadUpdateAction>> map = new HashMap<>();

    protected boolean reload;
    protected boolean softReload;

    PublishRelay<List<ThreadUpdateAction>> publishRelay = PublishRelay.create();

    public UpdateActionBatcher(long intervalMillis) {
        dm.add(Observable.interval(intervalMillis, TimeUnit.MILLISECONDS).observeOn(RX.computation()).subscribe(aLong -> {
            process();
        }));
    }

    public void addReload() {
        map.clear();
        reload = true;
        softReload = false;
    }

    public void addSoftReload() {
        softReload = true;
    }

    public void addAction(ThreadUpdateAction action) {
        if (!reload) {
            Map<ThreadUpdateAction.Type, ThreadUpdateAction> innerMap = map.get(action.thread);
            if (innerMap == null) {
                innerMap = new HashMap<>();
                map.put(action.thread, innerMap);
            }
            Logger.debug("Add action: " + action.type);
            innerMap.put(action.type, action);
        }
    }

    protected void process() {
        List<ThreadUpdateAction> toExecute = new ArrayList<>();
        if (reload) {
            toExecute.add(ThreadUpdateAction.reload());
        } else {
            if (softReload) {
                toExecute.add(ThreadUpdateAction.softReload());
            }
            if (!map.isEmpty()) {

                Map<Thread, Map<ThreadUpdateAction.Type, ThreadUpdateAction>> actionMap = new HashMap<>(map);
                map.clear();

                for (Thread thread: actionMap.keySet()) {
                    Map<ThreadUpdateAction.Type, ThreadUpdateAction> innerMap = actionMap.get(thread);
                    if (innerMap != null && !innerMap.isEmpty()) {
                        List<ThreadUpdateAction> actions = new ArrayList<>(innerMap.values());
                        Collections.sort(actions, (o1, o2) -> o2.priority().compareTo(o1.priority()));

                        // If we have a remove event, then ignore other events
                        if (actions.get(0).type == ThreadUpdateAction.Type.Remove) {
                            toExecute.add(actions.get(0));
                        } else {
                            toExecute.addAll(actions);
                        }
                    }
                }
            }
            if (!toExecute.isEmpty()) {
                publishRelay.accept(toExecute);
            }
        }
        reload = false;
        softReload = false;
    }

    public void dispose() {
        dm.dispose();
    }

    public PublishRelay<List<ThreadUpdateAction>> onUpdate() {
        return publishRelay;
    }

}
