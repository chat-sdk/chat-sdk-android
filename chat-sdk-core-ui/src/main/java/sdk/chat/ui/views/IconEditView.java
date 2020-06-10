package sdk.chat.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;

public class IconEditView extends LinearLayout {

    @BindView(R2.id.imageView) protected ImageView imageView;
    @BindView(R2.id.textInput) protected TextInputEditText textInput;
    @BindView(R2.id.textInputLayout) protected TextInputLayout textInputLayout;
    @BindView(R2.id.root) protected LinearLayout root;

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
        LayoutInflater.from(getContext()).inflate(R.layout.view_icon_edit, this, true);
        ButterKnife.bind(this);
    }

    public void setIcon(Drawable icon) {
        imageView.setImageDrawable(icon);
    }

    public void setText(String text) {
        textInput.setText(text);
    }

    public void setNextFocusDown(int id) {
        textInput.setNextFocusDownId(id);
    }

    public String getText() {
        Editable editable = textInput.getText();
        if (editable != null) {
            return editable.toString().trim();
        }
        return "";
    }

    public void setHint(@StringRes int resId) {
        textInputLayout.setHint(getContext().getResources().getString(resId));
    }

    public void setInputType(int type) {
        textInput.setInputType(type);
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

    public void setEnabled(boolean enabled) {
        textInput.setFocusable(!enabled);
        textInput.setClickable(!enabled);
        textInput.setEnabled(enabled);
    }

}
