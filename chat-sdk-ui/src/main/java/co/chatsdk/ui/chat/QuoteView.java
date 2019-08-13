package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;

import co.chatsdk.ui.R;

public class QuoteView extends LinearLayout {

    public TextView quotedUsername;
    public TextView quotedText;
    public SimpleDraweeView quotedImageView;
    public WeakReference<TextInputDelegate> delegate;

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
        inflate(getContext(), R.layout.quote_view, this);
    }

    protected void initViews() {
        quotedUsername = findViewById(R.id.quoted_user_name);
        quotedText = findViewById(R.id.quoted_text_content);
        quotedImageView = findViewById(R.id.quoted_image_view);
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
