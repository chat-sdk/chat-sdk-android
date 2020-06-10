package sdk.chat.ui.utils;

import android.content.res.ColorStateList;

import androidx.annotation.ColorInt;

public class ColorStateListUtil {

    public static ColorStateList forColor(@ColorInt int color) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
        };

        int[] colors = new int[]{
                color,
        };

        return new ColorStateList(states, colors);
    }


}
