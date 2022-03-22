package app.xmpp.adapter.activities;

import android.os.Bundle;
import android.view.View;

import app.xmpp.adapter.R;
import app.xmpp.adapter.R2;
import app.xmpp.adapter.fragments.XMPPConfigureFragment;
import butterknife.BindView;
import sdk.chat.ui.activities.BaseActivity;

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
