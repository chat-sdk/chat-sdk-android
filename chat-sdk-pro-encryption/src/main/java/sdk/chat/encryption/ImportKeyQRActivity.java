package sdk.chat.encryption;

import android.os.Bundle;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.activities.BaseActivity;

public class ImportKeyQRActivity extends BaseActivity {

    protected CodeScannerView scannerView;
    protected CodeScanner scanner;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        scannerView = findViewById(R.id.scanner_view);
        scanner = new CodeScanner(this, scannerView);
        scanner.setAutoFocusEnabled(true);
        scanner.setCamera(CodeScanner.CAMERA_BACK);
        scanner.setFormats(CodeScanner.ALL_FORMATS);

        scanner.setDecodeCallback(result -> {
            if (!StringChecker.isNullOrEmpty(result.getText())) {
                try {
                    EncryptionHandler.importKeyPair(result.getText());
                } catch (Exception e) {
                    showToast(e.getLocalizedMessage());
                }
            }
            finish();
        });
        scanner.setErrorCallback(error -> {
            showToast(error.getLocalizedMessage());
        });
        scannerView.setOnClickListener(v -> {
            scanner.startPreview();
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_qr_scanner;
    }

    @Override
    public void onResume() {
        super.onResume();
        scanner.startPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanner.releaseResources();
    }

}
