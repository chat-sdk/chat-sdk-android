package sdk.chat.ui.chat.options;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.reactivex.Completable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.guru.common.DisposableMap;

/**
 * Created by ben on 10/11/17.
 */

public class BaseChatOption implements ChatOption {

    protected Action action;
    protected @StringRes int title;
    protected @DrawableRes int image;
    protected DisposableMap dm = new DisposableMap();

    public BaseChatOption (@StringRes int title, @DrawableRes int image, Action action) {
        this.action = action;
        this.title = title;
        this.image = image;
    }

    @Override
    public @DrawableRes int getImage() {
        return image;
    }

    @Override
    public @StringRes int getTitle() {
        return title;
    }

    @Override
    public Completable execute(Activity activity, ActivityResultLauncher<Intent> launcher, Thread thread) {
        if(action != null) {
            return action.execute(activity, launcher, thread);
        }
        return Completable.complete();
    }

    public interface Action {
        Completable execute(Activity activity, ActivityResultLauncher<Intent> launcher, Thread thread);
    }

    protected void dispose () {
        dm.dispose();
    }

    @Override
    public AbstractKeyboardOverlayFragment getOverlay(KeyboardOverlayHandler sender) {
        return null;
    }

    @Override
    public boolean hasOverlay() {
        return false;
    }

}
