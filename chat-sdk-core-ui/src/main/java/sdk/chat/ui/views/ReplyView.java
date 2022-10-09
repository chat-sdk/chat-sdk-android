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
import sdk.chat.ui.icons.Icons;

public class ReplyView extends ConstraintLayout {

    protected ImageView imageView;
    protected View divider;
    protected TextView replyTextView;
    protected ImageButton cancelButton;
    protected ConstraintLayout root;

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

        imageView = findViewById(R.id.imageView);
        divider = findViewById(R.id.divider);
        replyTextView = findViewById(R.id.replyTextView);
        cancelButton = findViewById(R.id.cancelButton);
        root = findViewById(R.id.root);
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
