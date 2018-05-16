package co.chatsdk.android.app;

import android.os.Bundle;

import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class CustomThreadDetailsActivity extends ThreadDetailsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_activity_thread_details);
    }

}
