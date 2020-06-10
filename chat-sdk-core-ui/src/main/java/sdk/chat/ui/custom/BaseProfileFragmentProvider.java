package sdk.chat.ui.custom;

import androidx.fragment.app.Fragment;

import sdk.chat.core.dao.User;
import sdk.chat.core.ui.ProfileFragmentProvider;
import sdk.chat.ui.fragments.ProfileFragment;

public class BaseProfileFragmentProvider implements ProfileFragmentProvider {
    @Override
    public Fragment profileFragment(User user) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setUser(user);
        return fragment;
    }
}
