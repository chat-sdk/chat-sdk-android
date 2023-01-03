package sdk.chat.ui.chat.options;

import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.StringRes;

public class ChatOptionBuilder {

    @StringRes
    int title;
    @DrawableRes int image;
    BaseChatOption.Action action;

    public ChatOptionBuilder () {

    }

    public ChatOptionBuilder title(@StringRes int title) {
        this.title = title;
        return this;
    }

    public ChatOptionBuilder image(@DrawableRes int image) {
        this.image = image;
        return this;
    }

    public ChatOptionBuilder action(BaseChatOption.Action action) {
        this.action = action;
        return this;
    }

    public BaseChatOption build () {
        return new BaseChatOption(title, image, action);
    }

}
