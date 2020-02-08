package co.chatsdk.ui.chat;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.databinding.ViewChatReplyBinding;

public class ReplyView extends ConstraintLayout {

    @BindView(R2.id.image_view) protected ImageView imageView;
    @BindView(R2.id.replyTextView) protected TextView textView;
    @BindView(R2.id.cancelButton) protected ImageButton cancelButton;

    protected ViewChatReplyBinding b;

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
        DataBindingUtil.inf

        inflate(getContext(), R.layout.view_chat_reply, this);

        ButterKnife.bind(this);

        hide();
    }

    public void show(String title, String imageURL, String text) {
        setVisibility(View.VISIBLE);

        if (imageURL != null && !imageURL.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(imageURL).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
        if (text != null) {
            textView.setText(Html.fromHtml("<b>" + title + "</b><br/>" + text));
        }
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void setOnCancelListener(View.OnClickListener listener) {
        cancelButton.setOnClickListener(listener);
    }

}
