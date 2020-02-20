package co.chatsdk.ui.chat.options;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by ben on 10/11/17.
 */

public class BaseChatOption implements ChatOption {

    protected Action action;
    protected String title;
    protected Drawable drawable;
    protected DisposableList disposableList = new DisposableList();

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
            return Icons.get(Icons.choose().add, R.color.white);
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
        disposableList.dispose();
    }
}
