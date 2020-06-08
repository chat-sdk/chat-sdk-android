package sdk.chat.message.sticker.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sdk.chat.message.sticker.R;

/**
 * Created by ben on 10/11/17.
 */

public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.ViewHolder> {

    ArrayList<StickerListItem> items = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageButton imageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.image_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_sdk_sticker_cell, null);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final StickerListItem listItem = items.get(position);

        holder.imageButton.setImageResource(listItem.getImageResourceId());
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItem.click();
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems (ArrayList<StickerListItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

}
