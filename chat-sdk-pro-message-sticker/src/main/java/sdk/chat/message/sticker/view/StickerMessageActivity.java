package sdk.chat.message.sticker.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import sdk.chat.core.dao.Keys;
import sdk.chat.message.sticker.R;
import sdk.chat.ui.activities.BaseActivity;

/**
 * Created by ben on 10/11/17.
 */

public class StickerMessageActivity extends BaseActivity {

    StickerMessageFragment fragment;

    @Override
    protected int getLayout() {
        return R.layout.activity_sticker;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = (StickerMessageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_sticker);
        fragment.setStickerResultListener(stickerName -> {
            Intent result = new Intent();
            result.putExtra(Keys.MessageStickerName, stickerName);
            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
