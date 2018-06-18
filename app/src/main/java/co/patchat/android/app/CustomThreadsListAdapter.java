package co.patchat.android.app;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.threads.ThreadViewHolder;
import co.chatsdk.ui.threads.ThreadsListAdapter;

public class CustomThreadsListAdapter extends ThreadsListAdapter {

    protected static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ChatSDK.config().messageTimeFormat);

    public CustomThreadsListAdapter(Context context) {
        super(context);
    }

    @Override
    public ThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.custom_row_thread, null);
        return new ThreadViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ThreadViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SpannableString spanString = new SpannableString(holder.unreadMessageCountTextView.getText().toString());
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        holder.unreadMessageCountTextView.setText(spanString);
    }

    @Override
    public String getLastMessageDateAsString(Date date) {
        if (simpleDateFormat != null) {
            return String.valueOf(simpleDateFormat.format(date));
        } else {
            return super.getLastMessageDateAsString(date);
        }
    }

}
