package co.chatsdk.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;

import co.chatsdk.ui.R;
import co.chatsdk.ui.databinding.ViewIconEditBinding;

public class IconEditView extends LinearLayout {

    protected ViewIconEditBinding b;

    public IconEditView(Context context) {
        super(context);
        initViews();
    }

    public IconEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public IconEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public void initViews() {
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_icon_edit, this, true);
    }

    public void setIcon(Drawable icon) {
        b.imageView.setImageDrawable(icon);
    }

    public void setText(String text) {
        b.textInput.setText(text);
    }

    public void setNextFocusDown(int id) {
        b.textInput.setNextFocusDownId(id);
    }

    public String getText() {
        Editable editable = b.textInput.getText();
        if (editable != null) {
            return editable.toString().trim();
        }
        return "";
    }

    public void setHint(@StringRes int resId) {
        b.textInputLayout.setHint(getContext().getResources().getString(resId));
    }

    public void setInputType(int type) {
        b.textInput.setInputType(type);
    }

    public static IconEditView create(Context context, String text, @DrawableRes int icon) {
        return create(context, text, context.getResources().getDrawable(icon));
    }

    public static IconEditView create(Context context, String text, Drawable icon) {
        IconEditView view = new IconEditView(context);
        view.setText(text);
        view.setIcon(icon);
        return view;
    }

}
