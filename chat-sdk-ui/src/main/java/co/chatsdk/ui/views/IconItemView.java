package co.chatsdk.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.databinding.DataBindingUtil;

import co.chatsdk.ui.R;
import co.chatsdk.ui.databinding.ViewIconItemBinding;

public class IconItemView extends LinearLayout {

    protected ViewIconItemBinding b;

    public IconItemView(Context context) {
        super(context);
        initViews();
    }

    public IconItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public IconItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public void initViews() {
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_icon_item, this, true);
    }

    public void setIcon(Drawable icon) {
        b.imageView.setImageDrawable(icon);
    }

    public void setText(String text) {
        b.textView.setText(text);
    }

    public static IconItemView create(Context context, String text, @DrawableRes int icon) {
        return create(context, text, context.getResources().getDrawable(icon));
    }

    public static IconItemView create(Context context, String text, Drawable icon) {
        IconItemView view = new IconItemView(context);
        view.setText(text);
        view.setIcon(icon);
        return view;
    }
}
