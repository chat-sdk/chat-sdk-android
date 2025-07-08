package sdk.chat.ui.extras;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.zxing.Result;

import io.reactivex.disposables.Disposable;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.utils.ToastHelper;

public class ScanQRCodeActivity extends Activity implements ZXingScannerView.ResultHandler {

    protected ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        mScannerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(mScannerView);                // Set the scanner view as the content view

    }

    @Override
    public void onResume() {
        super.onResume();
        Disposable d = PermissionRequestHandler.requestCameraAccess(this).subscribe(() -> {
            mScannerView.setResultHandler(ScanQRCodeActivity.this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        if (!StringChecker.isNullOrEmpty(rawResult.getText())) {
            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, rawResult.getText());
            ChatSDK.contact().addContact(user, ConnectionType.Contact).doOnComplete(() -> {
                ToastHelper.show(this, sdk.chat.core.R.string.contact_added);
            }).doOnError(throwable -> {
                ToastHelper.show(this, throwable.getLocalizedMessage());
            }).subscribe();
        }
        finish();
    }

}
