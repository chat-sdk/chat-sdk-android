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

import com.squareup.picasso.Picasso;

import co.chatsdk.ui.R;

public class ReplyView extends ConstraintLayout {

    protected ImageView imageView;
    protected TextView textView;
    protected ImageButton cancelButton;

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
        inflate(getContext(), R.layout.view_chat_reply, this);

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.replyTextView);
        cancelButton = findViewById(R.id.cancelButton);

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
