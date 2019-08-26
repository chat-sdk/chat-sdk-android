package co.chatsdk.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
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

public class SendMultipleImagesAtOnceActivity extends BaseActivity {

    private String imageURI;
    private RecyclerView multipleImageRecycler;
    private RecyclerView.LayoutManager layoutManager;

    public PhotoView mainImageDisplay;
    public Uri mainImageUri;
    public ImageListAdapter imageListAdapter;
    public File fileBeingCropped;
    public Uri uriBeingCropped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_multiple_images);

        TextView topTextView = findViewById(R.id.topTextView);
        TextView bottomTextView = findViewById(R.id.bottomTextView);
        FloatingActionButton exit = findViewById(R.id.exit);
        FloatingActionButton write = findViewById(R.id.write);
        FloatingActionButton add = findViewById(R.id.add);
        FloatingActionButton send = findViewById(R.id.send);
        mainImageDisplay = (PhotoView) findViewById(R.id.main_displayed_image);

        //Recycler view stuff here
        multipleImageRecycler = (RecyclerView) findViewById(R.id.multipleImageRecycler);
        multipleImageRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        multipleImageRecycler.setLayoutManager(layoutManager);

        //Setting the image which was selected here.
        Intent i = getIntent();
        imageURI = (String)i.getSerializableExtra("uriString");
        Uri uri = Uri.parse(imageURI);
        mainImageDisplay.setImageURI(uri);
        mainImageUri = uri;

        //Adapter here
        imageListAdapter = new ImageListAdapter(this);
        multipleImageRecycler.setAdapter(imageListAdapter);
        imageListAdapter.addImageToMyAdapter(uri);


        //When the add button is clicked, you are taken to the library.
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaSelector mediaSelector = new MediaSelector();
                disposableList.add(mediaSelector.startChooseImageActivity(SendMultipleImagesAtOnceActivity.this, MediaSelector.CropType.Rectangle)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
            }
        });
        //This might be too complicated, but I will get to that later.
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                ArrayList<String> listOfUriStrings = new ArrayList<>();
                for (int i = 0; i <= imageListAdapter.uriArrayList.size(); i++) {
                    Uri uri = imageListAdapter.uriArrayList.get(i);
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

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                uriBeingCropped = mainImageUri;
                Cropper.startActivity(SendMultipleImagesAtOnceActivity.this, uriBeingCropped);
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO) {
                File file = MediaSelector.fileFromURI(data.getData(), this, MediaStore.Images.Media.DATA);
                Uri uri = data.getData();
                if (!imageListAdapter.uriArrayList.contains(uri)) {
                    imageListAdapter.uriArrayList.add(uri);
                    mainImageUri = uri;
                    imageListAdapter.notifyDataSetChanged();
                }
                setTheMainImageDisplay(uri);
            }
            else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                fileBeingCropped = MediaSelector.fileFromURI(result.getUri(), this, MediaStore.Images.Media.DATA);
                Uri uri = result.getUri();
                imageListAdapter.uriArrayList.add(uri);
                setTheMainImageDisplay(uri);
                mainImageUri = uri;
                imageListAdapter.removeImageFromAdapter(uriBeingCropped);
                imageListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setTheMainImageDisplay(Uri uri) {
        mainImageDisplay.setImageURI(uri);
        mainImageDisplay.setAdjustViewBounds(true);
    }

    public void setTheMainImageUri(Uri uri) {
        mainImageUri = uri;
    }
}