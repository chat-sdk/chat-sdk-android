package co.chatsdk.ui.utils;

import android.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ben on 10/4/17.
 */

public class ViewCollapser {

    public static void verticalHide (View v, boolean vertical) {
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        v.requestLayout();
    }

    public static void horizontalHide (View v, boolean vertical) {
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        v.requestLayout();
    }

    public static void verticalShow (View v, boolean vertical) {
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.requestLayout();
    }

    public static void horizontalShow (View v, boolean vertical) {
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.requestLayout();
    }

}
