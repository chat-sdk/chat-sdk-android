package co.chatsdk.android.app.custom;

import android.os.Bundle;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.profile.ProfileFragment;

public class AProfileFragment extends ProfileFragment {

    public static ProfileFragment newInstance(User user) {
        AProfileFragment f = new AProfileFragment();

        Bundle b = new Bundle();

        if (user != null) {
            b.putString(Keys.UserId, user.getEntityID());
        }

        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

}
