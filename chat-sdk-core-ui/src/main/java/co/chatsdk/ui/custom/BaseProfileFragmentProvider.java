package co.chatsdk.ui.custom;

import androidx.fragment.app.Fragment;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.ui.ProfileFragmentProvider;
import co.chatsdk.ui.fragments.ProfileFragment;

public class BaseProfileFragmentProvider implements ProfileFragmentProvider {
    @Override
    public Fragment profileFragment(User user) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setUser(user);
        return fragment;
    }
}
