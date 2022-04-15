package sdk.chat.app.xmpp;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.widget.RelativeLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


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

    public CustomLayout(Context context) {
        super(context, null);
    }

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

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



    public int getKeyboardHeight() {
        return 100;
    }

    public boolean isLandscape() {
        int rotation = getDeviceRotation();
        return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
    }

    private int getDeviceRotation() {
        if (Build.VERSION.SDK_INT >= 30) {
            getContext().getDisplay().getRealMetrics(displayMetrics);
        } else {
        }
        return displayMetrics.widthPixels > displayMetrics.heightPixels ? Surface.ROTATION_90 : Surface.ROTATION_0;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

}
