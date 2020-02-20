package co.chatsdk.ui.views;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import com.squareup.picasso.Picasso;
import co.chatsdk.ui.R;
import co.chatsdk.ui.databinding.ViewChatReplyBinding;
import co.chatsdk.ui.icons.Icons;

public class ReplyView extends ConstraintLayout {

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
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.view_chat_reply, this, true);
        b.cancelButton.setImageDrawable(Icons.get(Icons.choose().cancel, R.color.gray_light));
        hide();
    }

    public void show(String title, String imageURL, String text) {
        setVisibility(View.VISIBLE);

        if (imageURL != null && !imageURL.isEmpty()) {
            b.imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(imageURL).into(b.imageView);
        } else {
            b.imageView.setVisibility(View.GONE);
        }
        if (text != null) {
            b.replyTextView.setText(Html.fromHtml("<b>" + title + "</b><br/>" + text));
        }
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void setOnCancelListener(View.OnClickListener listener) {
        b.cancelButton.setOnClickListener(listener);
    }

}
