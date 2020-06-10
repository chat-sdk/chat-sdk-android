package sdk.chat.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;

public class IconItemView extends LinearLayout {

    @BindView(R2.id.imageView) protected ImageView imageView;
    @BindView(R2.id.textView) protected TextView textView;
    @BindView(R2.id.root) protected LinearLayout root;

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
        LayoutInflater.from(getContext()).inflate(R.layout.view_icon_item, this, true);
        ButterKnife.bind(this);
    }

    public void setIcon(Drawable icon) {
        imageView.setImageDrawable(icon);
    }

    public void setText(String text) {
        textView.setText(text);
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
