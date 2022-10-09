package sdk.chat.ui.activities.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lassi.data.media.MiMedia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PreviewPagerAdapter extends FragmentStateAdapter {

    protected ArrayList<MiMedia> media = new ArrayList<>();

    protected boolean isVideo = false;

    public PreviewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PreviewFragment fragment = new PreviewFragment();
        MiMedia item = media.get(position);
        fragment.media = item;
        return fragment;
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    public void addMedia(List<MiMedia> media) {
        for (MiMedia item: media) {
            if (!contains(item)) {
                this.media.add(item);
                notifyItemInserted(this.media.indexOf(item));
            }
            isVideo = item.getDuration() > 0;
        }
    }

    public boolean isVideo() {
        return isVideo;
    }

    public boolean contains(MiMedia item) {
        if (item == null) {
            return false;
        }
        for (MiMedia m: media) {
            if (m.getId() == item.getId()) {
                return true;
            }
        }
        return false;
    }

//    public void removeItem(MiMedia item) {
//        media.remove(item);
//    }

    public void removeItemAtIndex(int index)  {
        if (index >= 0 && index < getItemCount()) {

//            removeItemAtIndex(index);
            media.remove(index);
            notifyItemRangeChanged(index, getItemCount());
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        return media.get(position).getId();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (MiMedia item: media) {
            if (item.getId() == itemId) {
                return true;
            }
        }
        return false;
    }

}
