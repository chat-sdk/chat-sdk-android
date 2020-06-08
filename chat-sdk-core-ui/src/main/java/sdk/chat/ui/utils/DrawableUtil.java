package sdk.chat.ui.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class DrawableUtil {

    public static Drawable getMessageSelector(Context context, int normalColor, int selectedColor, int pressedColor, int shape) { //@DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(context, getDrawable(context, shape))).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_selected},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
                        },
                        new int[]{getColor(context, selectedColor), getColor(context, pressedColor), getColor(context, normalColor)}
                ));
        return drawable;
    }

    public static Drawable getVectorDrawable(Context context, @DrawableRes int drawable) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public static @ColorInt int getColor(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return  typedValue.data;
    }

    public static @DrawableRes int getDrawable(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return  typedValue.resourceId;
    }

}
