package co.chatsdk.firebase.nearby_users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import co.chatsdk.ui.contacts.UsersListAdapter;

/**
 * Created by pepe on 17.10.18.
 */

public class NearbyUsersListAdapter extends UsersListAdapter {

    protected class UserViewHolder extends UsersListAdapter.UserViewHolder {
        protected TextView distanceTextView;

        public UserViewHolder(View view) {
            super(view);
            this.distanceTextView = view.findViewById(R.id.tvDistance);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View row = inflater.inflate(R.layout.chat_sdk_row_header, null);
            return new HeaderViewHolder(row);
        }
        else if (viewType == TYPE_USER) {
            View row = inflater.inflate(R.layout.chat_sdk_row_nearby_user, null);
            return new UserViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (getItemViewType(position) == TYPE_USER) {
            UserViewHolder uh = (UserViewHolder) holder;
            LocationUser user = (LocationUser)items.get(position);
            uh.distanceTextView.setText(user.getDistanceText());
        }
    }

}
