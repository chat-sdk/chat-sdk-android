package co.chatsdk.profile.pictures;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import androidx.annotation.LayoutRes;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ToastHelper;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by Pepe on 01/12/19.
 */

public class ProfilePicturesActivity extends BaseActivity {

    protected User user;
    protected GridLayout gridLayout;
    protected MediaSelector mediaSelector = new MediaSelector();

    private DisposableList disposableList = new DisposableList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userEntityID = getIntent().getStringExtra(InterfaceManager.USER_ENTITY_ID);

        if (userEntityID != null && !userEntityID.isEmpty()) {
            user = StorageManager.shared().fetchUserWithEntityID(userEntityID);
            if (user == null) {
                ToastHelper.show(this, R.string.user_entity_id_not_set);
                finish();
                return;
            }
        }

        setContentView(activityLayout());

        initViews();
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.chat_sdk_profile_pictures_activity;
    }

    protected void initViews() {
        gridLayout = findViewById(R.id.gridLayout);
    }

    protected View createCellView(String url) {
        SimpleDraweeView cell = new SimpleDraweeView(this);
        cell.setImageURI(url);
        cell.setOnClickListener(v -> {
            if (getUser().isMe()) {

            }
        });
        if (getUser().isMe()) {
            cell.setOnLongClickListener(v -> {
                boolean isDefault = ChatSDK.profilePictures().fromUser(getUser()).indexOf(url) == 0;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (!isDefault) {
                    builder.setTitle(getString(R.string.set_as_default));
                    builder.setPositiveButton(getString(R.string.set_as_default), (dialog, which) -> {
                        showOrUpdateProgressDialog(getString(R.string.updating_pictures));
                        ChatSDK.profilePictures().setDefaultPicture(user, url);
                        disposableList.add(ChatSDK.core().pushUser().subscribe(() -> {
                            dismissProgressDialog();
                            updateGallery();
                        }));
                    });
                } else {
                    builder.setTitle(getString(R.string.action_delete_picture));
                }
                builder.setNegativeButton(getString(R.string.delete), (dialog, which) -> {
                    showOrUpdateProgressDialog(getString(R.string.deleting_picture));
                    ChatSDK.profilePictures().removePicture(user, url);
                    disposableList.add(ChatSDK.core().pushUser().subscribe(() -> {
                        dismissProgressDialog();
                        updateGallery();
                    }));
                });
                builder.setNeutralButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            });
        }
        return cell;
    }

    protected void addCellToGridLayout(GridLayout gridLayout, View cell) {
        if (cell != null) {
            gridLayout.addView(cell);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) cell.getLayoutParams();
            int margin = 8;
            int size = gridLayout.getWidth() / gridLayout.getColumnCount() - margin * 2;
            params.topMargin = margin;
            params.leftMargin = margin;
            params.rightMargin = margin;
            params.bottomMargin = margin;
            params.width = size;
            params.height = size;
            cell.setLayoutParams(params);
        }
    }

    protected void updateGallery() {
        ArrayList<String> urls = ChatSDK.profilePictures().fromUser(getUser());
        gridLayout.removeAllViews();
        for (String url : urls) {
            addCellToGridLayout(gridLayout, createCellView(url));
        }
    }

    protected void addProfilePicture() {
        mediaSelector.startChooseImageActivity(this, MediaSelector.CropType.Circle, result -> {
            try {
                File compress = new Compressor(ChatSDK.shared().context())
                        .setMaxHeight(ChatSDK.config().imageMaxThumbnailDimension)
                        .setMaxWidth(ChatSDK.config().imageMaxThumbnailDimension)
                        .compressToFile(new File(result));

                Bitmap bitmap = BitmapFactory.decodeFile(compress.getPath());

                // Cache the file
                File file = ImageUtils.compressImageToFile(ChatSDK.shared().context(), bitmap, getUser().getEntityID(), ".png");

                String imagePath = Uri.fromFile(file).toString();
                ChatSDK.profilePictures().addPicture(getUser(), imagePath);
                updateGallery();

                showOrUpdateProgressDialog(getString(R.string.uploading_picture));
                if (ChatSDK.profilePictures().fromUser(getUser()).size() == 1) {
                    // If it is the first picture, upload the default user avatar using ChatSDK.core().push()
                    disposableList.add(ChatSDK.core().pushUser()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                dismissProgressDialog();
                                updateGallery();
                            }));
                } else {
                    // If it is the not first picture, upload the picture using uploadImage()
                    disposableList.add(uploadImage(imagePath)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                dismissProgressDialog();
                                updateGallery();
                            }));
                }
            } catch (Exception e) {
                ChatSDK.logError(e);
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected User getUser() {
        return user != null ? user : ChatSDK.currentUser();
    }

    protected Completable uploadImage(String path) {
        return Completable.create(e -> {
            File avatar = new File(new URI(path).getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(avatar.getPath());
            if (bitmap != null && ChatSDK.upload() != null) {
                ChatSDK.upload().uploadImage(bitmap).subscribe(new Observer<FileUploadResult>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {}

                    @Override
                    public void onNext(@NonNull FileUploadResult fileUploadResult) {
                        if (fileUploadResult.urlValid()) {
                            ChatSDK.profilePictures().replacePicture(ChatSDK.currentUser(), path, fileUploadResult.url);
                            ChatSDK.currentUser().update();
                            ChatSDK.events().source().onNext(NetworkEvent.userMetaUpdated(ChatSDK.currentUser()));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable ex) {
                        ChatSDK.logError(ex);
                        e.onError(ex);
                    }

                    @Override
                    public void onComplete() {
                        e.onComplete();
                    }
                });
            } else {
                ToastHelper.show(ChatSDK.shared().context(), "Error: bitmap is null");
                e.onComplete();
            }
        }).concatWith(ChatSDK.core().pushUser());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            mediaSelector.handleResult(this, requestCode, resultCode, data);
        }
        catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            ChatSDK.logError(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGallery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        updateGallery();
        if (!getUser().isMe())
            return super.onCreateOptionsMenu(menu);

        MenuItem item = menu.add(Menu.NONE, R.id.action_chat_sdk_add, 12, getString(R.string.action_add_picture));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add) {
            addProfilePicture();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposableList.dispose();
    }

}
