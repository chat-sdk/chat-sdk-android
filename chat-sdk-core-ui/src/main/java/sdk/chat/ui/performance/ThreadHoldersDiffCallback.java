package sdk.chat.ui.performance;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import sdk.chat.ui.chat.model.ThreadHolder;

public class ThreadHoldersDiffCallback extends AbstractDiffCallback<ThreadHolder> {

    public ThreadHoldersDiffCallback(List<ThreadHolder> oldList, List<ThreadHolder> newList) {
        super(oldList, newList);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ThreadHolder oldHolder = oldList.get(oldItemPosition);
        ThreadHolder newHolder = newList.get(newItemPosition);
        return oldHolder.getThread().equalsEntity(newHolder.getThread());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ThreadHolder newHolder = newList.get(newItemPosition);
        if (newHolder.isDirty()) {
            newHolder.markClean();
            return false;
        }
        return true;

//        ThreadHolder oldHolder = oldThreadHolderList.get(oldItemPosition);
//        return oldHolder.contentsIsEqual(newHolder);
    }
}
