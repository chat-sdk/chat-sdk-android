package sdk.chat.ui.chat.options;

import android.graphics.drawable.Drawable;

public class ChatOptionBuilder {

    String title;
    Drawable drawable;
    BaseChatOption.Action action;

    public ChatOptionBuilder () {

    }

    public ChatOptionBuilder icon(Drawable iconDrawable) {
        this.drawable = iconDrawable;
        return this;
    }

    public ChatOptionBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ChatOptionBuilder action(BaseChatOption.Action action) {
        this.action = action;
        return this;
    }

    public BaseChatOption build () {
        return new BaseChatOption(title, drawable, action);
    }

}
