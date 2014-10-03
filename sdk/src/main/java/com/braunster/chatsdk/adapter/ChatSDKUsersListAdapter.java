package com.braunster.chatsdk.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BUser;

import java.util.List;

/**
 * Created by itzik on 6/16/2014.
 */
public class ChatSDKUsersListAdapter extends ChatSDKAbstractUsersListAdapter<ChatSDKAbstractUsersListAdapter.UserListItem> {

    private static final String TAG = ChatSDKUsersListAdapter.class.getSimpleName();
    private static final boolean DEBUG = Debug.UsersWithStatusListAdapter;

    public ChatSDKUsersListAdapter(Activity activity) {
        super(activity);
    }

    public ChatSDKUsersListAdapter(Activity activity, boolean isMultiSelect) {
        super(activity, isMultiSelect);
    }

    public ChatSDKUsersListAdapter(Activity activity, List<UserListItem> listData) {
        super(activity, listData);
    }

    public ChatSDKUsersListAdapter(Activity activity, List<UserListItem> listData, boolean multiSelect) {
        super(activity, listData, multiSelect);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        row = view;

        final ViewHolder holder;
        final UserListItem userItem = userItems.get(position);

        // If the row is null or the View inside the row is not good for the current item.
        if (row == null || userItem.getResourceID() != row.getId())
        {
            holder  = new ViewHolder();
            row =  rowForType(holder, position);
        }
        else holder = (ViewHolder) row.getTag();

        holder.textView.setText(userItem.getText());

        if (textColor!=-1991)
            holder.textView.setTextColor(textColor);

        if (getItemViewType(position) == TYPE_USER)
        {
           if (userItem.fromURL)
            {
                int size = holder.profilePicture.getHeight();

                if (userItem.pictureThumbnailURL != null )
                {
                    if (holder.profilePicLoader!=null)
                        holder.profilePicLoader.kill();

                    holder.profilePicLoader = new ProfilePicLoader(holder.profilePicture);

                    VolleyUtils.getImageLoader().get(userItem.pictureThumbnailURL, holder.profilePicLoader, size, size);
                }
                else holder.profilePicture.setImageResource(R.drawable.ic_profile);
            }
            else
            {
                if (DEBUG) Log.i(TAG, "Loading profile picture from the db");

                Bitmap bitmap = userItems.get(position).getPicture();
                if (bitmap != null)
                {
                    holder.profilePicture.setImageBitmap(bitmap);
                }
                else
                {
                    holder.profilePicture.setImageResource(R.drawable.ic_profile);
                }
            }

            if (profilePicClickListener!=null)
            {
                holder.profilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profilePicClickListener.onClick(v, userItem.asBUser());
                    }
                });
            }
        }

        return row;
    }

    @Override
    public void initMaker() {
        itemMaker = new UserListItemMaker<UserListItem>() {
            @Override
            public UserListItem fromBUser(BUser user) {
                UserListItem item =  new UserListItem(R.layout.chat_sdk_row_contact,
                        user.getEntityID(),
                        user.getMetaName(),
                        user.getThumbnailPictureURL(),
                        user.getMetaPictureUrl(),
                        TYPE_USER);
                return item;
            }

            @Override
            public UserListItem getHeader(String type) {
                return new UserListItem(com.braunster.chatsdk.R.layout.chat_sdk_list_header, type, TYPE_HEADER);
            }
        };
    }
}