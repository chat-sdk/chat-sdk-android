package sdk.chat.ui.views;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.icons.Icons;

public class ReplyView extends ConstraintLayout {

    @BindView(R2.id.imageView) protected ImageView imageView;
    @BindView(R2.id.divider) protected View divider;
    @BindView(R2.id.replyTextView) protected TextView replyTextView;
    @BindView(R2.id.cancelButton) protected ImageButton cancelButton;
    @BindView(R2.id.root) protected ConstraintLayout root;

    public ReplyView(Context context) {
        super(context);
        initViews();
    }

    public ReplyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ReplyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    protected void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_chat_reply, this, true);
        ButterKnife.bind(this);

        cancelButton.setImageDrawable(Icons.get(getContext(), Icons.choose().cancel, R.color.gray_light));
        hide();
    }

    public void show(String title, @Nullable String imageURL, String text) {
        setVisibility(View.VISIBLE);

        if (imageURL != null && !imageURL.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageURL)
                    .dontAnimate()
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
        if (text != null) {
            replyTextView.setText(Html.fromHtml("<b>" + title + "</b><br/>" + text));
        }
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void setOnCancelListener(OnClickListener listener) {
        cancelButton.setOnClickListener(listener);
    }

}
