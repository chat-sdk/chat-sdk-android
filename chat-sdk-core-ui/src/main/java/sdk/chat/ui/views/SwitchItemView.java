package sdk.chat.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.utils.ColorStateListUtil;

public class SwitchItemView extends LinearLayout {

    @BindView(R2.id.imageView) protected ImageView imageView;
    @BindView(R2.id.textView) protected TextView textView;
    @BindView(R2.id.switchMaterial) protected SwitchCompat switchMaterial;
    @BindView(R2.id.root) protected LinearLayout root;

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
        LayoutInflater.from(getContext()).inflate(R.layout.view_switch_item, this, true);
        ButterKnife.bind(this);
    }

    public void setTrackColor(@ColorInt int color) {
        switchMaterial.setTrackTintList(ColorStateListUtil.forColor(color));
    }

    public void setThumbColor(@ColorInt int color) {
        switchMaterial.setThumbTintList(ColorStateListUtil.forColor(color));
    }

    public void setSelected(boolean selected) {
        switchMaterial.setChecked(selected);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setListener(CompoundButton.OnCheckedChangeListener listener) {
        switchMaterial.setOnCheckedChangeListener(listener);
    }

    public void setIcon(Drawable icon) {
        imageView.setImageDrawable(icon);
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