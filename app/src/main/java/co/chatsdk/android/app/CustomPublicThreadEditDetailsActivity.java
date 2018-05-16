package co.chatsdk.android.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.threads.PublicThreadEditDetailsActivity;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class CustomPublicThreadEditDetailsActivity extends PublicThreadEditDetailsActivity {

    final int CHOOSE_FILE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_activity_edit_public_thread_details);

        Button uploadPDFButton = findViewById(R.id.chat_sdk_edit_thread_pdf_btn);
        uploadPDFButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*" );
            Intent chooser = Intent.createChooser(intent, "Choose a file");
            startActivityForResult(chooser, CHOOSE_FILE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_FILE && resultCode == RESULT_OK) {
            // Get filePath and fileName
            Uri fileUri = data.getData();
            String filePath = fileUri.getPath();
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

            // Get mimeType
            String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
            String mimeType = "application/octet-stream";
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }

            // Get fileData
            byte[] fileData = "File content".getBytes(); //FileUtils.fileToBytes(new File(filePath));

            Log.d("UPLOADING", fileName);

            NM.upload().uploadFile(fileData, fileName, mimeType).subscribe(new Observer<FileUploadResult>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(FileUploadResult result) {
                    if (!StringChecker.isNullOrEmpty(result.url)) {
                        EditText input = findViewById(R.id.chat_sdk_edit_thread_pdf_et);
                        input.setText(result.url);
                        Timber.v("ProgressListener: " + result.progress.asFraction());
                    }
                }

                @Override
                public void onError(Throwable ex) {
                    Log.e("UPLOAD", ex.getLocalizedMessage());
                }

                @Override
                public void onComplete() {
                    Log.d("UPLOADED", fileName);
                }
            });
        } else {
            Log.d("FILE", "NO FILE");
        }
    }
}
