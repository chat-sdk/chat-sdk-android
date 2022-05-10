package sdk.chat.ui.fragments;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import sdk.chat.ui.chat.model.ThreadHolder;

public class ThreadHoldersDiffCallback extends DiffUtil.Callback {

    protected final List<ThreadHolder> oldThreadHolderList;
    protected final List<ThreadHolder> newThreadHolderList;

    public ThreadHoldersDiffCallback(List<ThreadHolder> oldList, List<ThreadHolder> newList) {
        oldThreadHolderList = oldList;
        newThreadHolderList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldThreadHolderList.size();
    }

    @Override
    public int getNewListSize() {
        return newThreadHolderList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ThreadHolder oldHolder = oldThreadHolderList.get(oldItemPosition);
        ThreadHolder newHolder = newThreadHolderList.get(newItemPosition);
        return oldHolder.getThread().equalsEntity(newHolder.getThread());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ThreadHolder oldHolder = oldThreadHolderList.get(oldItemPosition);
        ThreadHolder newHolder = newThreadHolderList.get(newItemPosition);
        return oldHolder.contentsIsEqual(newHolder);
    }
}
