package sdk.chat.ui.extras;

import android.os.Bundle;
import android.widget.ImageView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.activities.BaseActivity;

public class ShowQRCodeActivity extends BaseActivity {

    protected ImageView imageView;
    protected String userEntityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userEntityID = getIntent().getStringExtra(Keys.IntentKeyUserEntityID);
        if (StringChecker.isNullOrEmpty(userEntityID)) {
            finish();
        }
        imageView = findViewById(R.id.qrCodeImageView);
        initViews();
    }

    protected void initViews() {
        try {
            QRGEncoder qrgEncoder = new QRGEncoder(userEntityID, null, QRGContents.Type.TEXT, 200);
            imageView.setImageBitmap(qrgEncoder.getBitmap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_qr_code;
    }

}
