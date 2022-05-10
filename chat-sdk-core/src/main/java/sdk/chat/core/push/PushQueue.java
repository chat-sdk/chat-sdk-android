package sdk.chat.core.push;

import java.util.ArrayList;
import java.util.List;

public class PushQueue {

    public List<PushQueueAction> queue = new ArrayList<>();

    public void add(PushQueueAction action) {
        queue.add(action);
    }

    public PushQueueAction first() {
        if (queue.size() > 0) {
            return queue.get(0);
        }
        return null;
    }

    public PushQueueAction pop() {
        PushQueueAction first = first();
        if (first != null) {
            queue.remove(first);
        }
        return first;
    }

}
