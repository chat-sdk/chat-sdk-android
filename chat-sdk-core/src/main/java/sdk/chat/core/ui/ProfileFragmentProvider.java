package sdk.chat.core.ui;

import androidx.fragment.app.Fragment;

import sdk.chat.core.dao.User;

public interface ProfileFragmentProvider {
    Fragment profileFragment (User user);
}
