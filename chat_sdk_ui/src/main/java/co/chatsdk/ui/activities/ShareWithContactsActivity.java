/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.ui.R;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by braunster on 27/07/14.
 */
public class ShareWithContactsActivity extends BaseActivity {

    public static final String TAG = ShareWithContactsActivity.class.getSimpleName();
    public static final boolean DEBUG = Debug.ShareWithContactsActivity;

    /*FIXME add a check to see if there is any user logged in, If there is not add an option for a quick login just for passing all the bundle.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_fragment_activity);

        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (StringUtils.isEmpty(action) && StringUtils.isEmpty(type))
        {
            showToast(getString(R.string.share_activity_error_getting_share_data));
            return;
        }

        if (!action.equals(Intent.ACTION_SEND))
        {
            showToast(getString(R.string.share_activity_error_getting_share_data));
            return;
        }

        if (getIntent().getExtras() != null)
        {
            Object extraData = null;

            if (type.equals("text/plain"))
            {
                extraData = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            }
            else if (type.startsWith("image/"))
            {
                extraData = getIntent().getExtras().get(Intent.EXTRA_STREAM);
            }

            ContactsFragment fragment = ContactsFragment.newInstance(ContactsFragment.MODE_LOAD_CONTACTS, ContactsFragment.CLICK_MODE_SHARE_CONTENT, extraData);
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        }
        else showToast(getString(R.string.share_activity_error_getting_share_data));
    }
}
