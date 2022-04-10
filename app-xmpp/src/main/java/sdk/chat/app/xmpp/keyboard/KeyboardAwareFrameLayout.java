package sdk.chat.app.xmpp.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class KeyboardAwareFrameLayout extends FrameLayout {

    public KeyboardAwareFrameLayout(@NonNull Context context) {
        super(context);
    }

    public KeyboardAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
