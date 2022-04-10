package sdk.chat.app.xmpp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Field;

import sdk.chat.app.xmpp.signal.ServiceUtil;
import sdk.chat.app.xmpp.signal.ViewUtil;

public class CustomLayout extends RelativeLayout {

    private final DisplayMetrics displayMetrics  = new DisplayMetrics();
    private boolean isBubble = false;

    public interface HeightUpdater {
        void setHeight(int height);
    }

    public Runnable keyboardShown;
    public Runnable keyboardHidden;
    public HeightUpdater heightUpdater;
    private int viewInset;

    private int minKeyboardSize;
    private int minCustomKeyboardSize;
    private int defaultCustomKeyboardSize;
    private int minCustomKeyboardTopMarginPortrait;
    private int minCustomKeyboardTopMarginLandscape;
    private int minCustomKeyboardTopMarginLandscapeBubble;
    private int statusBarHeight;

    public CustomLayout(Context context) {
        super(context, null);
    }

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        minKeyboardSize                           = getResources().getDimensionPixelSize(R.dimen.min_keyboard_size);
        minCustomKeyboardSize                     = getResources().getDimensionPixelSize(R.dimen.min_custom_keyboard_size);
        defaultCustomKeyboardSize                 = getResources().getDimensionPixelSize(R.dimen.default_custom_keyboard_size);
        minCustomKeyboardTopMarginPortrait        = getResources().getDimensionPixelSize(R.dimen.min_custom_keyboard_top_margin_portrait);
        minCustomKeyboardTopMarginLandscape       = getResources().getDimensionPixelSize(R.dimen.min_custom_keyboard_top_margin_portrait);
        minCustomKeyboardTopMarginLandscapeBubble = getResources().getDimensionPixelSize(R.dimen.min_custom_keyboard_top_margin_landscape_bubble);
        statusBarHeight                           = ViewUtil.getStatusBarHeight(this);
        viewInset                                 = getViewInset();

    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updateKeyboardState();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updateKeyboardState() {
        Insets in = ViewCompat.getRootWindowInsets(this).getInsets(WindowInsetsCompat.Type.ime());
        if (in.bottom > 0) {
            if (heightUpdater != null) {
                int keyboad = in.bottom;
                Insets status = ViewCompat.getRootWindowInsets(this).getInsets(WindowInsetsCompat.Type.statusBars());
                Insets nav = ViewCompat.getRootWindowInsets(this).getInsets(WindowInsetsCompat.Type.navigationBars());
                int height = getKeyboardHeight();
//                heightUpdater.setHeight(height);

                int kh = in.bottom - nav.bottom;
                heightUpdater.setHeight(kh);
            }
            if (keyboardShown != null) {
                keyboardShown.run();
            }
        } else {
            if (keyboardHidden != null) {
                keyboardHidden.run();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getViewInset() {
        try {
            Field attachInfoField = View.class.getDeclaredField("mAttachInfo");
            attachInfoField.setAccessible(true);
            Object attachInfo = attachInfoField.get(this);
            if (attachInfo != null) {
                Field stableInsetsField = attachInfo.getClass().getDeclaredField("mStableInsets");
                stableInsetsField.setAccessible(true);
                Rect insets = (Rect) stableInsetsField.get(attachInfo);
                if (insets != null) {
                    return insets.bottom;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Do nothing
        }
        return statusBarHeight;
    }

    public int getKeyboardHeight() {
        return isLandscape() ? getKeyboardLandscapeHeight() : getKeyboardPortraitHeight();
    }

    public boolean isLandscape() {
        int rotation = getDeviceRotation();
        return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
    }

    private int getDeviceRotation() {
        if (Build.VERSION.SDK_INT >= 30) {
            getContext().getDisplay().getRealMetrics(displayMetrics);
        } else {
            ServiceUtil.getWindowManager(getContext()).getDefaultDisplay().getRealMetrics(displayMetrics);
        }
        return displayMetrics.widthPixels > displayMetrics.heightPixels ? Surface.ROTATION_90 : Surface.ROTATION_0;
    }

    private int getKeyboardLandscapeHeight() {
        if (isBubble) {
            return getRootView().getHeight() - minCustomKeyboardTopMarginLandscapeBubble;
        }

        int keyboardHeight = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("keyboard_height_landscape", defaultCustomKeyboardSize);
        return clamp(keyboardHeight, minCustomKeyboardSize, getRootView().getHeight() - minCustomKeyboardTopMarginLandscape);
    }

    private int getKeyboardPortraitHeight() {
        int keyboardHeight = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("keyboard_height_portrait", defaultCustomKeyboardSize);
        return clamp(keyboardHeight, minCustomKeyboardSize, getRootView().getHeight() - minCustomKeyboardTopMarginPortrait);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

}
