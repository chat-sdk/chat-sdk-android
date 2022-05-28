package sdk.chat.message.audio.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TouchAwareConstraintLayout extends ConstraintLayout {

    public interface TouchListener {
        void touchDown(float x, float y);
        void touchUp(float x, float y);
        void touchCancelled();
        void touchMoved(float x, float y);
    }

    public TouchListener listener;

    public TouchAwareConstraintLayout(@NonNull Context context) {
        super(context);
    }

    public TouchAwareConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchAwareConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

//        Logger.info("Touch " + ev.getAction());
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */
        if (listener != null) {
            final int action = ev.getAction();
            float x = ev.getRawX();
            float y = ev.getY();

            if (action == MotionEvent.ACTION_DOWN) {
                listener.touchDown(x, y);
            }
            if (action == MotionEvent.ACTION_UP) {
                listener.touchUp(x, y);
            }
            if (action == MotionEvent.ACTION_CANCEL) {
                listener.touchCancelled();
            }
            if (action == MotionEvent.ACTION_MOVE) {
                listener.touchMoved(x, y);
            }
        }

        // In general, we don't want to intercept touch events. They should be
        // handled by the child view.
        return true;
    }
}
