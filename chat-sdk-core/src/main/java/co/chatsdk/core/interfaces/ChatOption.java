package co.chatsdk.core.interfaces;

import android.app.Activity;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.types.ChatOptionType;
import co.chatsdk.core.utils.ActivityResult;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ben on 10/11/17.
 */

public interface ChatOption {

    int getIconResourceId ();
    String getTitle();
    Observable<?> execute (Activity activity, PublishSubject<ActivityResult> result, Thread thread);
    ChatOptionType getType ();

}
