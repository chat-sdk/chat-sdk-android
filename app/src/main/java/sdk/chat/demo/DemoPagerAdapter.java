package sdk.chat.demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.minidns.record.A;

import java.util.ArrayList;
import java.util.List;

public class DemoPagerAdapter extends FragmentStatePagerAdapter {

    public List<Fragment> fragments = new ArrayList<>();

    public DemoPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object item)
    {
        int index = fragments.indexOf(item);
        if (index >=0 && index < 3) {
            return POSITION_UNCHANGED;
        }
        return POSITION_NONE;
    }
}
