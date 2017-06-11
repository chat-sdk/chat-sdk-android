package co.chatsdk.ui.threads;

import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import co.chatsdk.ui.R;
import co.chatsdk.ui.UiHelpers.MakeThreadImage;
import co.chatsdk.ui.view.CircleImageView;

/**
 * Created by benjaminsmiley-andrews on 07/06/2017.
 */

public class ThreadViewHolder {

    public TextView txtName;
    public TextView txtDate;
    public TextView txtLastMsg;
    public TextView txtUnreadMessagesAmount;
    public CircleImageView imgIcon;
    public View indicator;

    public void setDefaultImg(AbstractThreadsListAdapter.ThreadListItem item){
        if (item.getUserCount() > 2)
            setMultipleUserDefaultImg();
        else
            setTwoUsersDefaultImg();
    }

    public void setMultipleUserDefaultImg(){
        imgIcon.setImageResource(R.drawable.ic_users);
    }

    public void setTwoUsersDefaultImg(){
        imgIcon.setImageResource(R.drawable.ic_profile);
    }

    public void showUnreadIndicator(){
        indicator.setVisibility(View.VISIBLE);
    }

    public void hideUnreadIndicator(){
        indicator.setVisibility(View.GONE);
    }

    public PicLoader picLoader;

    public PicLoader initPicLoader(AbstractThreadsListAdapter.ThreadListItem threadListItem){
        if (picLoader!=null)
            picLoader.kill();

        picLoader = new PicLoader(threadListItem);
        return picLoader;
    }



    class PicLoader implements ImageLoader.ImageListener {

        private boolean killed = false;

        private AbstractThreadsListAdapter.ThreadListItem threadListItem;


        PicLoader(AbstractThreadsListAdapter.ThreadListItem threadListItem) {
            this.threadListItem = threadListItem;
        }

        private void kill() {
            this.killed = true;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

            if (killed)
                return;

            // If response was not immidate, i.e  image was cached we show the default image while loading
            if (isImmediate && response.getBitmap() == null)
            {
                setDefaultImg(threadListItem);
                return;
            }

            // Set the response to the image.
            if (response.getBitmap() != null) {
                // load image into imageview
                imgIcon.setImageBitmap(response.getBitmap());
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (killed)
                return;

            // in case of error we show the default.
            setDefaultImg(threadListItem);
        }
    }

    public MakeThreadImage makeThreadImage;
}