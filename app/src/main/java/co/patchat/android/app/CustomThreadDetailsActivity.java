package co.patchat.android.app;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class CustomThreadDetailsActivity extends ThreadDetailsActivity {

    protected boolean isAdmin = false;

    @Override
    protected void initViews() {
        setContentView(R.layout.custom_activity_thread_details);
        super.initViews();
    }

    @Override
    protected void updateMetaData() {
        super.updateMetaData();

        TextView buildingLabel = findViewById(R.id.chat_sdk_thread_building_tv);
        TextView cityLabel = findViewById(R.id.chat_sdk_thread_city_tv);
        Button pdfButton = findViewById(R.id.pdf_attachment_btn);

        ThreadMetaValue buildingMetaValue = thread.metaValueForKey("building");
        ThreadMetaValue cityMetaValue = thread.metaValueForKey("city");
        ThreadMetaValue pdfMetaValue = thread.metaValueForKey("pdf");

        if (buildingMetaValue != null)
            buildingLabel.setText(buildingMetaValue.getValue());

        if (cityMetaValue != null)
            cityLabel.setText(cityMetaValue.getValue());

        if (pdfMetaValue != null && !pdfMetaValue.getValue().isEmpty()) {
            pdfButton.setEnabled(true);
            pdfButton.setVisibility(View.VISIBLE);
            pdfButton.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfMetaValue.getValue()));
                startActivity(browserIntent);
            });
        } else {
            pdfButton.setEnabled(false);
            pdfButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAdmin = NM.currentUser().metaBooleanForKey("admin");
        if (settingsItem != null) {
            settingsItem.setEnabled(isAdmin);
            settingsItem.setVisible(isAdmin);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Boolean result = super.onCreateOptionsMenu(menu);
        if (settingsItem != null) {
            settingsItem.setEnabled(isAdmin);
            settingsItem.setVisible(isAdmin);
        }
        return result;
    }
}
