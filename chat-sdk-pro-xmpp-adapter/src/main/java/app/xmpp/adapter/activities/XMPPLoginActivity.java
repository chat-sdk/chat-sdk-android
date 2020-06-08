package co.chatsdk.xmpp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import sdk.chat.core.utils.StringChecker;
import co.chatsdk.ui.activities.LoginActivity;
import co.chatsdk.xmpp.R;
import co.chatsdk.xmpp.R2;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.module.XMPPModule;
import co.chatsdk.xmpp.utils.XMPPServer;
import co.chatsdk.xmpp.utils.XMPPServerDetails;

public class XMPPLoginActivity extends LoginActivity {

    @BindView(R2.id.advancedConfigurationButton)
    MaterialButton advancedConfigurationButton;
    @BindView(R2.id.usernameSubtitleTextView)
    TextView usernameSubtitleTextView;

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_xmpp_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initViews() {
        super.initViews();

        advancedConfigurationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, XMPPConfigureActivity.class);
            startActivity(intent);
        });

        if (XMPPModule.config().allowServerConfiguration) {
            advancedConfigurationButton.setVisibility(View.VISIBLE);
        } else {
            advancedConfigurationButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSubtitle();
    }

    public void updateSubtitle() {
        XMPPServer server = XMPPManager.getCurrentServer(this);
        if (server != null && server.isValid()) {
            usernameSubtitleTextView.setText(String.format(getString(R.string.connecting_to__as__), server.address, server.resource));
        } else {
            usernameSubtitleTextView.setText(getString(R.string.no_server_specificed));
        }
    }


    protected boolean checkFields() {
        boolean valid = super.checkFields();
        if (valid) {
            // Check that there is a valid XMPP server

            // These values will be overridden if the user enters a fully qualified
            // username like user@domain.com:port/resource
            if (!StringChecker.isNullOrEmpty(usernameTextInput.getText())) {
                String username = usernameTextInput.getText().toString();

                try {
                    // Get the current server
                    XMPPServer server = XMPPManager.getCurrentServer(this);
                    XMPPServerDetails details = new XMPPServerDetails(username);

                    if (server == null) {
                        server = details.getServer();
                    }

                    usernameTextInput.setText(details.getUser());

                    // If the server is not hard coded or configured in defaults, then update it from the user
                    if (server.isValid()) {
                        XMPPManager.setCurrentServer(this, server);
                    } else {
                        showToast(R.string.xmpp_server_must_be_specified);
                        return false;
                    }

                    updateSubtitle();

                } catch (Exception e) {
                    showToast(e.getLocalizedMessage());
                    return false;
                }
            }
        }
        return valid;
    }

}
