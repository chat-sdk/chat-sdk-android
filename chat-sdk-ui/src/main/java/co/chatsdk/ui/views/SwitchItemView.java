package co.chatsdk.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import co.chatsdk.ui.R;
import co.chatsdk.ui.databinding.ViewSwitchItemBinding;

public class SwitchItemView extends LinearLayout {

    protected ViewSwitchItemBinding b;

    public SwitchItemView(Context context) {
        super(context, null);
        initViews();
    }

    public SwitchItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public SwitchItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public void initViews() {
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_switch_item, this, true);
    }

    public void setTrackColor(@ColorInt int color) {
//        int[][] states = new int[][] {
//                new int[] { android.R.attr.state_enabled}, // enabled
//                new int[] {-android.R.attr.state_enabled}, // disabled
//                new int[] {-android.R.attr.state_checked}, // unchecked
//                new int[] { android.R.attr.state_pressed}  // pressed
//        };
//
//        int[] colors = new int[] {
//                color,
//                Color.RED,
//                Color.GREEN,
//                Color.BLUE
//        };
        b.switchMaterial.setTrackTintList(listForColor(color));
    }

    public void setThumbColor(@ColorInt int color) {
        b.switchMaterial.setThumbTintList(listForColor(color));
    }

    public ColorStateList listForColor(@ColorInt int color) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };

        int[] colors = new int[] {
                color,
        };

        return new ColorStateList(states, colors);
    }

    public void setSelected(boolean selected) {
        b.switchMaterial.setChecked(selected);
    }

    public void setText(String text) {
        b.textView.setText(text);
    }

    public void setListener(CompoundButton.OnCheckedChangeListener listener) {
        b.switchMaterial.setOnCheckedChangeListener(listener);
    }

    public void setIcon(Drawable icon) {
        b.imageView.setImageDrawable(icon);
    }

    public static SwitchItemView create(Context context, @StringRes int text, Drawable icon, boolean selected, CompoundButton.OnCheckedChangeListener listener) {
        return create(context, text, icon, selected, 0, 0, listener);
    }

    public static SwitchItemView create(Context context, @StringRes int text, Drawable icon, boolean selected, @ColorRes int trackColor, @ColorRes int thumbColor, CompoundButton.OnCheckedChangeListener listener) {
        SwitchItemView view = new SwitchItemView(context);
        view.setText(context.getString(text));
        view.setSelected(selected);
        view.setListener(listener);
        view.setIcon(icon);
        if (trackColor != 0) {
            view.setTrackColor(ContextCompat.getColor(context, trackColor));
        }
        if (thumbColor != 0) {
            view.setThumbColor(ContextCompat.getColor(context, thumbColor));
        }
        return view;
    }


}