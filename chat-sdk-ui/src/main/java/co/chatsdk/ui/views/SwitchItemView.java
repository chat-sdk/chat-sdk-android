package co.chatsdk.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.StringRes;
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

    public static SwitchItemView create(Context context, @StringRes int text, Drawable icon, boolean selected, int switchTheme, CompoundButton.OnCheckedChangeListener listener) {
        SwitchItemView view = new SwitchItemView(context, null, switchTheme);
        view.setText(context.getString(text));
        view.setSelected(selected);
        view.setListener(listener);
        view.setIcon(icon);
        return view;
    }


}