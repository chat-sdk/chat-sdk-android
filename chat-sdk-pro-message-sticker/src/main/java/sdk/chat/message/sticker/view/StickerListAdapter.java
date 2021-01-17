package sdk.chat.message.sticker.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.message.sticker.R;

/**
 * Created by ben on 10/11/17.
 */

public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.ViewHolder> {

    ArrayList<StickerListItem> items = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
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

        if (listItem.isAnimated()) {
            Glide.with(holder.imageView).asGif().load(listItem.getIcon()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(listItem.getIcon());
        }

        holder.imageView.setOnClickListener(view -> listItem.click());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<StickerListItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

}
