package sdk.chat.core.interfaces;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import sdk.chat.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by ben on 10/11/17.
 */

public interface ChatOption {

    Drawable getIconDrawable();
    String getTitle();
    Completable execute(Activity activity, Thread thread);

}
