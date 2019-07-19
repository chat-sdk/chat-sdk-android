package co.chatsdk.core.ui;

import androidx.fragment.app.Fragment;
import co.chatsdk.core.dao.User;

public interface ProfileFragmentProvider {
    Fragment profileFragment (User user);
}
