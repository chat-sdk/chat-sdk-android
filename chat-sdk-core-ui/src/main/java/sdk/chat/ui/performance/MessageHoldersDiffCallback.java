package sdk.chat.ui.performance;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageWrapper;

import java.util.Date;
import java.util.List;

public class MessageHoldersDiffCallback extends AbstractDiffCallback<MessageWrapper<?>> {

    public MessageHoldersDiffCallback(List<MessageWrapper<?>> oldList, List<MessageWrapper<?>> newList) {
        super(oldList, newList);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        MessageWrapper<?> oldWrapper = super.oldList.get(oldItemPosition);
        MessageWrapper<?> newWrapper = super.newList.get(newItemPosition);
        return oldWrapper.equals(newWrapper);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

        MessageWrapper<?> oldWrapper = super.oldList.get(oldItemPosition);
        MessageWrapper<?> newWrapper = super.newList.get(newItemPosition);

        if (oldWrapper.item instanceof Date && newWrapper.item instanceof Date) {
            return ((Date) oldWrapper.item).equals(newWrapper.item);
        } else if (newWrapper.item instanceof IMessage) {
            IMessage item = (IMessage) newWrapper.item;
            if (item.isDirty()) {
                item.makeClean();
                return true;
            }
        }
        return false;
    }
}
