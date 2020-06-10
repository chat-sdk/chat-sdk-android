package sdk.chat.ui.chat.options;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.interfaces.ChatOption;
import sdk.guru.common.DisposableMap;
import sdk.chat.ui.icons.Icons;
import io.reactivex.Completable;

/**
 * Created by ben on 10/11/17.
 */

public class BaseChatOption implements ChatOption {

    protected Action action;
    protected String title;
    protected Drawable drawable;
    protected DisposableMap dm = new DisposableMap();

    public BaseChatOption (String title, Drawable drawable, Action action) {
        this.action = action;
        this.title = title;
        this.drawable = drawable;
    }

    public BaseChatOption (String title, Action action, MediaType type) {
        this(title, null, action);
    }

    @Override
    public Drawable getIconDrawable() {
        if (drawable != null) {
            return drawable;
        } else {
            return Icons.get(Icons.choose().add, Icons.shared().chatOptionIconColor);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Completable execute(Activity activity, Thread thread) {
        if(action != null) {
            return action.execute(activity, thread);
        }
        return Completable.complete();
    }

    public interface Action {
        Completable execute(Activity activity, Thread thread);
    }

    protected void dispose () {
        dm.dispose();
    }
}
