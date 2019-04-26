package co.chatsdk.ui.chat.options;

import android.app.Activity;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.utils.DisposableList;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by ben on 10/11/17.
 */

public class BaseChatOption implements ChatOption {

    protected Action action;
    protected String title;
    protected Integer iconResourceId;
    protected DisposableList disposableList = new DisposableList();

    public BaseChatOption (String title, Integer iconResourceId, Action action) {
        this.action = action;
        this.title = title;
        this.iconResourceId = iconResourceId;
    }

    public BaseChatOption (String title, Action action, MediaType type) {
        this(title, null, action);
    }

    @Override
    public int getIconResourceId() {
        return iconResourceId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Observable<?> execute(Activity activity, Thread thread) {
        if(action != null) {
            return action.execute(activity, thread);
        }
        return Completable.complete().toObservable();
    }

    public interface Action {
        Observable<?> execute(Activity activity, Thread thread);
    }

    protected void dispose () {
        disposableList.dispose();
    }
}
