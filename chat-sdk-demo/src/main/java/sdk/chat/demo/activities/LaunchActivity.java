package sdk.chat.demo.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import sdk.chat.demo.R;

public class LaunchActivity extends AbstractDemoActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_launch;
    }

    @Override
    protected void next() {

    }
}
