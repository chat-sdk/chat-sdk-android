package sdk.chat.ui.extras;

import android.os.Bundle;
import android.widget.ImageView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.activities.BaseActivity;

public class ShowQRCodeActivity extends BaseActivity {

    protected ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageView = findViewById(R.id.qrCodeImageView);
        initViews();
    }

    protected void initViews() {
        try {
            QRGEncoder qrgEncoder = new QRGEncoder(ChatSDK.currentUserID(), null, QRGContents.Type.TEXT, 200);
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
