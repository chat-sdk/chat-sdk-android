package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

import co.chatsdk.ui.R;

public class QuoteView extends LinearLayout {

    public TextView quotedUsername;
    public TextView quotedText;
    public SimpleDraweeView quotedImageView;
    public WeakReference<TextInputDelegate> delegate;
    public String quotedImageUri;
    public FloatingActionButton closeQuoteButton;

    public QuoteView(Context context) {
        super(context);
        init();
    }

    public QuoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuoteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setDelegate (TextInputDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    protected void init(){
        inflate(getContext(), R.layout.view_quote, this);
    }

    protected void initViews() {
        quotedUsername = findViewById(R.id.quotedUsername);
        closeQuoteButton = findViewById(R.id.closeQuoteButton);
        quotedImageView = findViewById(R.id.quotedImage);
        quotedText = findViewById(R.id.quotedText);
    }

    protected Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();

        if (isInEditMode()) {
            return;
        }
    }
}
