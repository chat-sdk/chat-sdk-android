package co.chatsdk.android.app.custom;

import co.chatsdk.ui.main.MainAppBarActivity;

public class AMainActivity extends MainAppBarActivity {

    @Override
    public void onBackPressed() {
        // Fixes an issue where if we press back the whole app goes blank
    }


}
