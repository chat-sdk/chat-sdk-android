package sdk.chat.core.ui;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public abstract class AbstractKeyboardOverlayFragment extends Fragment {

    @NonNull
    protected WeakReference<KeyboardOverlayHandler> keyboardOverlayHandler;

    public AbstractKeyboardOverlayFragment() {
    }

    public void setHandler(@NonNull KeyboardOverlayHandler handler) {
        keyboardOverlayHandler = new WeakReference<>(handler);
    }

    public abstract void setViewSize(Integer width, Integer height, Context context);

    public Boolean isPortrait() {
        if (getContext() != null) {
            int orientation = getContext().getResources().getConfiguration().orientation;
            return orientation == Configuration.ORIENTATION_PORTRAIT;
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public abstract String key();

}
