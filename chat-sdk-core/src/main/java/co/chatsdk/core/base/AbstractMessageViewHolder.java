package co.chatsdk.core.base;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import io.reactivex.subjects.PublishSubject;

public abstract class AbstractMessageViewHolder extends RecyclerView.ViewHolder {

    protected WeakReference<Activity> activity;
    protected Message message;

    protected View.OnClickListener onClickListener = null;
    protected View.OnLongClickListener onLongClickListener = null;
    protected PublishSubject<List<MessageAction>> actionPublishSubject;


    public AbstractMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView);
        this.activity = new WeakReference<>(activity);
        this.actionPublishSubject = actionPublishSubject;
    }

    public abstract void showProgressBar();
    public abstract void showProgressBar(float progress);
    public abstract void hideProgressBar ();

    public abstract void setIconSize(int width, int height);
    public abstract void setImageSize(int width, int height);
    public abstract void setBubbleHidden (boolean hidden);
    public abstract void setIconHidden (boolean hidden);
    public abstract void setImageHidden (boolean hidden);
    public abstract void setTextHidden (boolean hidden);
    public abstract View viewForClassType (Class classType);
    public abstract void setAlpha (float alpha);
    public abstract LinearLayout getExtraLayout();

    public void setMessage (Message message) {
        this.message = message;
    }

    public void setOnClickListener (View.OnClickListener listener) {
        onClickListener = listener;
    }

    public void setOnLongClickListener (View.OnLongClickListener listener) {
        onLongClickListener = listener;
    }

}
