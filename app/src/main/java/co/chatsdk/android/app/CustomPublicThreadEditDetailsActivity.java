package co.chatsdk.android.app;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.threads.PublicThreadEditDetailsActivity;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class CustomPublicThreadEditDetailsActivity extends PublicThreadEditDetailsActivity {

    final int CHOOSE_FILE = 42;

    protected EditText buildingInput;
    protected EditText cityInput;
    protected EditText pdfInput;

    @Override
    protected void initViews() {
        setContentView(R.layout.custom_activity_edit_public_thread_details);
        super.initViews();

        buildingInput = findViewById(R.id.chat_sdk_edit_thread_building_et);
        cityInput = findViewById(R.id.chat_sdk_edit_thread_city_et);
        pdfInput = findViewById(R.id.chat_sdk_edit_thread_pdf_et);

        ThreadMetaValue buildingMetaValue = thread.metaValueForKey("building");
        ThreadMetaValue cityMetaValue = thread.metaValueForKey("city");
        ThreadMetaValue pdfMetaValue = thread.metaValueForKey("pdf");

        if (buildingMetaValue != null)
            buildingInput.setText(buildingMetaValue.getValue());

        if (cityMetaValue != null)
            cityInput.setText(cityMetaValue.getValue());

        if (pdfMetaValue!= null)
            pdfInput.setText(pdfMetaValue.getValue());

        Button uploadPDFButton = findViewById(R.id.chat_sdk_edit_thread_pdf_btn);
        uploadPDFButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*" );
            Intent chooser = Intent.createChooser(intent, "Choose a file");
            startActivityForResult(chooser, CHOOSE_FILE);
        });
    }

    @Override
    protected void setSaveButtonOnClickListener() {
        final String threadName = nameInput.getText().toString();
        if (thread == null) {
            showOrUpdateProgressDialog(getString(co.chatsdk.ui.R.string.add_public_chat_dialog_progress_message));

            disposableList.add(NM.publicThread().createPublicThreadWithName(threadName)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((_thread, throwable) -> {
                        if (throwable == null) {
                            thread.setMetaValue("building", buildingInput.getText().toString());
                            thread.setMetaValue("city", cityInput.getText().toString());
                            thread.setMetaValue("pdf", pdfInput.getText().toString());
                            disposableList.add(new ThreadWrapper(thread).pushMeta().subscribe(() -> {
                                dismissProgressDialog();
                                ToastHelper.show(ChatSDK.shared().context(), String.format(getString(co.chatsdk.ui.R.string.public_thread__is_created), threadName));

                                InterfaceManager.shared().a.startChatActivityForID(ChatSDK.shared().context(), _thread.getEntityID());
                            }));
                        } else {
                            ChatSDK.logError(throwable);
                            Toast.makeText(ChatSDK.shared().context(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            dismissProgressDialog();
                        }
                    }));
        } else {
            thread.setName(threadName);
            thread.update();
            thread.setMetaValue("building", buildingInput.getText().toString());
            thread.setMetaValue("city", cityInput.getText().toString());
            thread.setMetaValue("pdf", pdfInput.getText().toString());
            disposableList.add(new ThreadWrapper(thread).pushMeta().subscribe(this::finish));
        }
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
