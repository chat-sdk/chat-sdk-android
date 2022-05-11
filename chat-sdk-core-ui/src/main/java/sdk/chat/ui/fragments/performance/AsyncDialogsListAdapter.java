package sdk.chat.ui.fragments.performance;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.List;

import sdk.chat.ui.chat.model.ThreadHolder;

public class AsyncDialogsListAdapter extends DialogsListAdapter<ThreadHolder> {

    protected AsyncListDiffer<ThreadHolder> asyncDiffer;

    public AsyncDialogsListAdapter(ImageLoader imageLoader) {
        super(imageLoader);
        setup();
    }

    public AsyncDialogsListAdapter(int itemLayoutId, ImageLoader imageLoader) {
        super(itemLayoutId, imageLoader);
        setup();
    }

    public AsyncDialogsListAdapter(int itemLayoutId, Class holderClass, ImageLoader imageLoader) {
        super(itemLayoutId, holderClass, imageLoader);
        setup();
    }

    public void setup() {

        asyncDiffer = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<ThreadHolder>() {
            @Override
            public boolean areItemsTheSame(@NonNull ThreadHolder oldItem, @NonNull ThreadHolder newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ThreadHolder oldItem, @NonNull ThreadHolder newItem) {
                return newItem.isDirty();
            }
        });


    }

    @Override
    public int getItemCount() {
        return asyncDiffer.getCurrentList().size();
    }//method to submit list

    public void submitList(List<ThreadHolder> data) {
        asyncDiffer.submitList(data);
    }

    public ThreadHolder getItem(int position) {
        return asyncDiffer.getCurrentList().get(position);
    }

    public List<ThreadHolder> getItems() {
        return asyncDiffer.getCurrentList();
    }


//    @Override
//    public ThreadHolder getItems() {
//
//    }

//    public void onBindViewHolder(ThreadHolder holder, int position) {
//        holder.setImageLoader(this.imageLoader);
//        holder.setOnDialogClickListener(this.onDialogClickListener);
//        holder.setOnDialogViewClickListener(this.onDialogViewClickListener);
//        holder.setOnLongItemClickListener(this.onLongItemClickListener);
//        holder.setOnDialogViewLongClickListener(this.onDialogViewLongClickListener);
//        holder.setDatesFormatter(this.datesFormatter);
//        holder.onBind(this.items.get(position));
//    }

}
