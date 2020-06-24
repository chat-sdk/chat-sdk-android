package sdk.chat.profile.pictures;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.image.ImageUploadResult;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.activities.ImagePreviewActivity;
import sdk.chat.ui.chat.MediaSelector;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ImagePickerUploader;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.RX;

/**
 * Created by Pepe on 01/12/19.
 */

public class ProfilePicturesActivity extends ImagePreviewActivity {

    protected User user;
    protected MenuItem addMenuItem;
    protected ImagePickerUploader imagePickerUploader = new ImagePickerUploader(MediaSelector.CropType.Circle);

    protected int gridPadding = 4;
    protected int pictureMargin = 8;
    protected int picturesPerRow = 2;
    protected int maxPictures = 6;
    protected boolean hideButton = false;
    protected String limitWarning = null;

    @BindView(R2.id.imageView) protected ImageView imageView;
    @BindView(R2.id.gridLayout) protected GridLayout gridLayout;
    @BindView(R2.id.root) protected LinearLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_profile_pictures;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        setActionBarTitle(R.string.profile);
        initViews();

    }

    @Override
    protected void setupViews() {
        super.setupViews();
        gridLayout.setPadding(gridPadding, gridPadding, gridPadding, gridPadding);
        gridLayout.setColumnCount(picturesPerRow);
    }

    protected View createCellView(String url) {
        ImageView cell = new ImageView(this);
        // Get the screen width
        int size = getResources().getDisplayMetrics().widthPixels / 2 - gridPadding;

        Glide.with(this).load(url)
                .placeholder(UIModule.config().defaultProfilePlaceholder)
                .error(UIModule.config().defaultProfilePlaceholder)
                .dontAnimate().override(size, size).centerCrop().into(cell);

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
                        dm.add(ChatSDK.core().pushUser().observeOn(RX.main()).subscribe(() -> {
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
                    dm.add(ChatSDK.core().pushUser().observeOn(RX.main()).subscribe(() -> {
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
        dm.add(PermissionRequestHandler.requestImageMessage(this).subscribe(() -> {
            dm.add(imagePickerUploader.choosePhoto(this, false).subscribe((results, throwable) -> {
                if (throwable != null) {
                    Toast.makeText(ProfilePicturesActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    for (ImageUploadResult result : results) {
                        ChatSDK.profilePictures().addPicture(getUser(), result.url);
                    }
                    updateGallery();
                    dm.add(ChatSDK.core().pushUser()
                            .observeOn(RX.main())
                            .subscribe(() -> {
                            }, this));
                }
            }));
        }, this));
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

        getMenuInflater().inflate(R.menu.add_menu, menu);
        addMenuItem = menu.findItem(R.id.action_add).setIcon(Icons.get(this, Icons.choose().add, Icons.shared().actionBarIconColor));
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
