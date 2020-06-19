package sdk.chat.demo.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import sdk.chat.demo.R;

public class InfoActivity extends AbstractDemoActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_info;
    }

    @Override
    protected void next() {
        Intent intent = new Intent(this, StyleActivity.class);
        startActivity(intent);
    }
}
