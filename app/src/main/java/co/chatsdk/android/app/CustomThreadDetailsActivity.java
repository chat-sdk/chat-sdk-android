package co.chatsdk.android.app;

import android.view.Menu;
import android.widget.TextView;

import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class CustomThreadDetailsActivity extends ThreadDetailsActivity {

    protected boolean isAdmin = false;

    @Override
    protected void initViews() {
        setContentView(R.layout.custom_activity_thread_details);
        super.initViews();

        updateMetaData();

        disposableList.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated))
                .subscribe(networkEvent -> updateMetaData()));
    }

    private void updateMetaData() {
        TextView buildingLabel = findViewById(R.id.chat_sdk_thread_building_tv);
        TextView cityLabel = findViewById(R.id.chat_sdk_thread_city_tv);
        TextView pdfLabel = findViewById(R.id.chat_sdk_thread_pdf_tv);

        ThreadMetaValue buildingMetaValue = thread.metaValueForKey("building");
        ThreadMetaValue cityMetaValue = thread.metaValueForKey("city");
        ThreadMetaValue pdfMetaValue = thread.metaValueForKey("pdf");

        if (buildingMetaValue != null)
            buildingLabel.setText(buildingMetaValue.getValue());

        if (cityMetaValue != null)
            cityLabel.setText(cityMetaValue.getValue());

        if (pdfMetaValue != null)
            pdfLabel.setText(pdfMetaValue.getValue());
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
