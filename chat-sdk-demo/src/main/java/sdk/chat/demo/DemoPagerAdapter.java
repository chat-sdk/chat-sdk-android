package sdk.chat.demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class DemoPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments = new ArrayList<>();

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

    public void setFragments(List<Fragment> fragments) {
        this.fragments.clear();
        this.fragments.addAll(fragments);
    }

    public void add(Fragment fragment) {
        fragments.add(fragment);
    }

    public List<Fragment> get() {
        return fragments;
    }
}
