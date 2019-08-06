package co.chatsdk.profile.pictures;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.utils.ImagePickerUploader;
import co.chatsdk.ui.utils.ImagePreviewActivity;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Pepe on 01/12/19.
 */

public class ProfilePicturesActivity extends ImagePreviewActivity {

    protected User user;
    protected GridLayout gridLayout;
    protected MenuItem addMenuItem;
    protected ImagePickerUploader imagePickerUploader = new ImagePickerUploader(MediaSelector.CropType.Circle);

    protected int gridPadding = 4;
    protected int pictureMargin = 8;
    protected int picturesPerRow = 2;
    protected int maxPictures = 6;
    protected boolean hideButton = false;
    protected String limitWarning = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userEntityID = getIntent().getStringExtra(Keys.IntentKeyUserEntityID);
        if (userEntityID != null && !userEntityID.isEmpty()) {
            user = ChatSDK.db().fetchUserWithEntityID(userEntityID);
            if (user == null) {
                ToastHelper.show(this, R.string.user_entity_id_not_set);
                finish();
                return;
            }
        }

        gridPadding = getIntent().getIntExtra(BaseProfilePicturesHandler.KeyGridPadding, gridPadding);
        pictureMargin = getIntent().getIntExtra(BaseProfilePicturesHandler.KeyPictureMargin, maxPictures);
        picturesPerRow = getIntent().getIntExtra(BaseProfilePicturesHandler.KeyPicturesPerRow, picturesPerRow);
        maxPictures = getIntent().getIntExtra(BaseProfilePicturesHandler.KeyMaxPictures, maxPictures);
        hideButton = getIntent().getBooleanExtra(BaseProfilePicturesHandler.KeyHideButton, hideButton);
        String warning = getIntent().getStringExtra(BaseProfilePicturesHandler.KeyLimitWarning);
        if (warning != null) {
            limitWarning = warning;
        }
    }

    @Override
    protected @LayoutRes int activityLayout() {
        return R.layout.chat_sdk_profile_pictures_activity;
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setPadding(gridPadding, gridPadding, gridPadding, gridPadding);
        gridLayout.setColumnCount(picturesPerRow);
    }

    protected View createCellView(String url) {
        SimpleDraweeView cell = new SimpleDraweeView(this);
        cell.setImageURI(url);
        cell.setOnClickListener(v -> {
            zoomImageFromThumbnail(cell, url);
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
            int size = gridLayout.getWidth() / gridLayout.getColumnCount() - pictureMargin * 2;
            params.topMargin = pictureMargin;
            params.leftMargin = pictureMargin;
            params.rightMargin = pictureMargin;
            params.bottomMargin = pictureMargin;
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

        if (addMenuItem != null) {
            addMenuItem.setVisible(shouldShowAddButton(urls));
        }
    }

    protected boolean shouldShowAddButton(List<String> urls) {
        return !hideButton || urls.size() < maxPictures;
    }

    protected void addProfilePicture() {
        if (ChatSDK.profilePictures().fromUser(getUser()).size() >= maxPictures && maxPictures > 0) {
            if (!limitWarning.isEmpty()) {
                ToastHelper.show(this, limitWarning);
            }
            return;
        }

        disposableList.add(imagePickerUploader.choosePhoto(this).subscribe((result, throwable) -> {
            if (throwable == null) {
                ChatSDK.profilePictures().addPicture(getUser(), result.url);
                updateGallery();
                disposableList.add(ChatSDK.core().pushUser()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, throwable1 -> {
                            // Handle Error
                            Toast.makeText(ProfilePicturesActivity.this, throwable1.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }));
            } else {
                Toast.makeText(ProfilePicturesActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    protected User getUser() {
        return user != null ? user : ChatSDK.currentUser();
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

        addMenuItem = menu.add(Menu.NONE, R.id.action_add, 12, getString(R.string.action_add_picture));
        addMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        addMenuItem.setIcon(R.drawable.ic_plus);
        addMenuItem.setVisible(shouldShowAddButton(ChatSDK.profilePictures().fromUser(getUser())));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_add) {
            addProfilePicture();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
