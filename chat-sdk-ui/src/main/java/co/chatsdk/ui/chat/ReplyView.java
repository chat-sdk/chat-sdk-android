package co.chatsdk.ui.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

import co.chatsdk.ui.R;

public class ReplyView extends ConstraintLayout {

    protected ImageView imageView;
    protected TextView textView;

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

//        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        inflater.inflate(R.layout.view_chat_reply, this, true);

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);

        hide();
    }

    public void show(String imageURL, String text) {
        setVisibility(View.VISIBLE);

        if (imageURL != null && !imageURL.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(imageURL).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
        if (text != null) {
            textView.setText(text);
        }
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

}
