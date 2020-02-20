package co.chatsdk.ui.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import co.chatsdk.ui.views.ChatView;

public class TextInputBehavior extends CoordinatorLayout.Behavior<View> {

    public TextInputBehavior() {
        super();
    }

    public TextInputBehavior(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public boolean layoutDependsOn (CoordinatorLayout parent,
                                    View child,
                                    View dependency){
        return dependency instanceof ChatView;
    }

    public boolean onDependentViewChanged (CoordinatorLayout parent,
                                           View child,
                                           View dependency){

        if (child instanceof LinearLayout && dependency instanceof ChatView) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, child.getHeight());
            dependency.setLayoutParams(params);
        }

        return false;
    }
}
