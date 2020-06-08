package co.chatsdk.xmpp.activities;

import android.os.Bundle;
import android.view.View;

import org.pmw.tinylog.Logger;

import butterknife.BindView;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.xmpp.R;
import co.chatsdk.xmpp.R2;
import co.chatsdk.xmpp.fragments.XMPPConfigureFragment;

public class XMPPConfigureActivity extends BaseActivity {

    @BindView(R2.id.fragment)
    View fragment;

    @Override
    protected int getLayout() {
        return R.layout.activity_xmpp_configure;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPConfigureFragment fragment = (XMPPConfigureFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null) {
            fragment.save();
        }
    }

}
