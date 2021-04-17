package sdk.chat.encryption;

import android.os.Bundle;
import android.widget.ImageView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import sdk.chat.ui.activities.BaseActivity;

public class ExportKeyQRActivity extends BaseActivity {

    protected ImageView imageView;
    protected String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            key = EncryptionHandler.exportKeyPair();
        } catch (Exception e) {
            showToast(e.getLocalizedMessage());
            finish();
        }
        imageView = findViewById(R.id.qrCodeImageView);
        initViews();
    }

    protected void initViews() {
        try {
            QRGEncoder qrgEncoder = new QRGEncoder(key, null, QRGContents.Type.TEXT, 200);
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
