package sdk.chat.ui.performance;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public abstract class AbstractDiffCallback<T>  extends DiffUtil.Callback {

    protected final List<T> oldList;
    protected final List<T> newList;

    public AbstractDiffCallback(List<T> oldList, List<T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

}
