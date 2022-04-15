package sdk.chat.core.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public abstract class AbstractKeyboardOverlayFragment extends Fragment {

    protected WeakReference<Activity> activity;

    @NonNull
    protected WeakReference<KeyboardOverlayHandler> keyboardOverlayHandler;

    public AbstractKeyboardOverlayFragment(@NonNull KeyboardOverlayHandler keyboardOverlayHandler) {
        this.keyboardOverlayHandler = new WeakReference<>(keyboardOverlayHandler);
    }

    public abstract void setViewSize(Integer width, Integer height, Resources resources);

    public void setActivity(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public boolean isPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
