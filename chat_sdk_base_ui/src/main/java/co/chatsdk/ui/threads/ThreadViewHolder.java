package co.chatsdk.ui.threads;

import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import co.chatsdk.ui.R;
import de.hdodenhof.circleimageview.CircleImageView;

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

    public void showUnreadIndicator(){
        indicator.setVisibility(View.VISIBLE);
    }

    public void hideUnreadIndicator(){
        indicator.setVisibility(View.GONE);
    }




}