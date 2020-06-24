package sdk.chat.demo.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import sdk.chat.demo.R;
import sdk.chat.demo.R2;
import sdk.chat.ui.activities.BaseActivity;

public abstract class AbstractDemoActivity extends BaseActivity {

    @BindView(R2.id.fab)
    protected FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> {
            fab.setEnabled(false);
            next();
        });
        fab.setImageResource(R.drawable.icons8_forward);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null) {
            View view = fragment.getView().findViewById(R.id.swipeTextView);
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
        }

        overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setEnabled(true);
    }

    protected void next() {

    }



}
