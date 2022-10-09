package sdk.chat.ui.keyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.module.UIModule;

public class KeyboardAwareFrameLayout extends FrameLayout {

    public interface HeightUpdater {
        void setHeight(int height);
    }

    public List<Runnable> keyboardShownListeners = new ArrayList<>();
    public List<Runnable> keyboardHiddenListeners = new ArrayList<>();

//    public Runnable keyboardShown;
//    public Runnable keyboardHidden;
    public HeightUpdater heightUpdater;

    public int keyboardHeight = 0;

    protected boolean keyboardOpen = false;

    public KeyboardAwareFrameLayout(@NonNull Context context) {
        super(context);
    }

    public KeyboardAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updateKeyboardState();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean keyboardOverlayAvailable() {
        return ViewCompat.getRootWindowInsets(this) != null && UIModule.config().keyboardOverlayEnabled;
    }

    private void updateKeyboardState() {
        WindowInsetsCompat wic = ViewCompat.getRootWindowInsets(this);
        if (wic != null) {
            Insets ime = wic.getInsets(WindowInsetsCompat.Type.ime());
            Insets sb = wic.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets nav = wic.getInsets(WindowInsetsCompat.Type.navigationBars());

            if (ime.bottom > 0) {
                if (heightUpdater != null) {
                    setKeyboardHeight(ime.bottom - nav.bottom);
                    heightUpdater.setHeight(keyboardHeight);
                }
                if (!keyboardOpen) {
                    for (Runnable r: keyboardShownListeners) {
                        r.run();
                    }
                }
            } else {
                if (keyboardOpen) {
                    for (Runnable r: keyboardHiddenListeners) {
                        r.run();
                    }
                }
            }

            keyboardOpen = ime.bottom > 0;
        }
    }

    public boolean isKeyboardOpen() {
        return keyboardOpen;
    }

    protected String orientationKey() {
        if (isPortrait()) {
            return Keys.KeyboardHeightPortrait;
        } else {
            return Keys.KeyboardHeightLandscape;
        }
    }

    public boolean isPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    protected void setKeyboardHeight(int height) {
        keyboardHeight = height;
        ChatSDK.shared()
                .getPreferences()
                .edit()
                .putInt(orientationKey(), height)
                .apply();
    }

    public int getKeyboardHeight() {
        if (keyboardHeight > 0) {
            return keyboardHeight;
        } else {
            int height = ChatSDK.shared().getPreferences().getInt(orientationKey(), -1);
            if (height > 0) {
                return height;
            } else {
                if (isPortrait()) {
                    return (int) Math.ceil(getMeasuredHeight() * 0.4);
                } else {
                    return (int) Math.ceil(getMeasuredHeight() * 0.6);
                }
            }
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

}
