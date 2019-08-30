package co.chatsdk.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;

import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.Cropper;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static co.chatsdk.ui.chat.MediaSelector.CHOOSE_PHOTO;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class ImageGalleryActivity extends BaseActivity {

    private String imageUriString;
    private ArrayList<Uri> uriArrayList;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public PhotoView currentImage;
    public Uri mainImageUri;
    public ImageListAdapter imageListAdapter;
    public File fileBeingCropped;
    public Uri uriBeingCropped;

    TextView topTextView;
    TextView bottomTextView;
    FloatingActionButton exitButton;
    FloatingActionButton cropButton;
    FloatingActionButton addButton;
    FloatingActionButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        topTextView = findViewById(R.id.topTextView);
        bottomTextView = findViewById(R.id.bottomTextView);
        exitButton = findViewById(R.id.exit);
        cropButton = findViewById(R.id.crop);
        addButton = findViewById(R.id.add);
        sendButton = findViewById(R.id.send);
        currentImage = findViewById(R.id.main_displayed_image);

        //Recycler view stuff here
        recyclerView = findViewById(R.id.multipleImageRecycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //Getting the data from the MediaSelector. All images will come as an array list of uris.
        Intent i = getIntent();
        uriArrayList = (ArrayList<Uri>)i.getSerializableExtra("uriArray");

        //Adapter here
        imageListAdapter = new ImageListAdapter(this);
        recyclerView.setAdapter(imageListAdapter);

        //Setting the image which was selected here.
        imageListAdapter.imageURIs = uriArrayList;
        Uri uri = uriArrayList.get(0);
        setTheMainImageDisplay(uri);

        //When the add button is clicked, you are taken to the library.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaSelector mediaSelector = new MediaSelector();
                disposableList.add(mediaSelector.startChooseImageActivity(ImageGalleryActivity.this, MediaSelector.CropType.Rectangle)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
            }
        });
        //This might be too complicated, but I will get to that later. Should I make lamda values out of these?
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                ArrayList<String> listOfUriStrings = new ArrayList<>();
                for (int i = 0; i <= imageListAdapter.imageURIs.size(); i++) {
                    Uri uri = imageListAdapter.imageURIs.get(i);
                    String uriString = uri.toString();
                    listOfUriStrings.add(uriString);
                }
                /*//This is the part where the project has to actually be integrated with the rest of the app.
                    Disposable d = ChatSDK.imageMessage().sendMessageWithImage(file, thread).subscribe(() -> {
                        // Handle Success
                    }, (Consumer<Throwable>) throwable -> {
                        // Handle failure
                    });*/

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", listOfUriStrings);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                uriBeingCropped = mainImageUri;
                Cropper.startActivity(ImageGalleryActivity.this, uriBeingCropped);
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO) {

                if (data.getData() != null) {
                    Uri uri = data.getData();
                    if (!imageListAdapter.imageURIs.contains(uri)) {
                        imageListAdapter.imageURIs.add(uri);
                        imageListAdapter.notifyDataSetChanged();
                        setTheMainImageDisplay(uri);
                    }
                }
                else if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int j = 0; j < mClipData.getItemCount(); j++) {
                        ClipData.Item item = mClipData.getItemAt(j);
                        Uri uri = item.getUri();
                        imageListAdapter.imageURIs.add(uri);
                    }
                    imageListAdapter.notifyDataSetChanged();
                }
            }
            else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                fileBeingCropped = MediaSelector.fileFromURI(result.getUri(), this, MediaStore.Images.Media.DATA);
                Uri uri = result.getUri();
                imageListAdapter.imageURIs.add(uri);
                setTheMainImageDisplay(uri);
                imageListAdapter.removeImageUri(uriBeingCropped);
                imageListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setTheMainImageDisplay(Uri uri) {
        currentImage.setImageURI(uri);
        currentImage.setAdjustViewBounds(true);
        mainImageUri = uri;
    }
}