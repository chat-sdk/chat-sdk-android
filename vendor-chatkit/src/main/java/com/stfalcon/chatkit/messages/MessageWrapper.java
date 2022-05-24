package com.stfalcon.chatkit.messages;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

public class MessageWrapper<DATA> {
    public DATA item;
    public boolean isSelected;

    MessageWrapper(DATA item) {
        this.item = item;
    }

    public boolean equals(MessageWrapper<DATA> wrapper) {
        return item.equals(wrapper.item);
    }

    public Date getDate() {
        if (item instanceof Date) {
            return (Date) item;
        }
        if (item instanceof IMessage) {
            return ((IMessage) item).getCreatedAt();
        }
        // Should never be hit
        assert(false);
        return new Date();
    }

    public int compare(MessageWrapper<?> wrapper) {
        if (wrapper != null && getDate() != null) {
            return getDate().compareTo(wrapper.getDate());
        }
        return 0;
    }

}
