package co.chatsdk.core.interfaces;

import android.app.Activity;

import co.chatsdk.core.dao.Thread;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by ben on 10/11/17.
 */

public interface ChatOption {

    int getIconResourceId ();
    String getTitle();
    Completable execute (Activity activity, Thread thread);

}
