package co.chatsdk.ui;

import android.app.Activity;
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

import java.io.File;
import java.util.ArrayList;

import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.main.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static co.chatsdk.ui.chat.MediaSelector.CHOOSE_PHOTO;

public class SendMultipleImagesAtOnceActivity extends BaseActivity {

    private String imageURI;
    private RecyclerView multipleImageRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public ArrayList<Uri> myImageArray = new ArrayList<>();
    //This crap has to be static or else it won't work in MyAdapter
    public static PhotoView mainImageDisplay;

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
        myImageArray.add(uri);

        //Adapter here
        mAdapter = new MyAdapter(myImageArray);
        multipleImageRecycler.setAdapter(mAdapter);

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
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO) {
                File file = MediaSelector.fileFromURI(data.getData(), this, MediaStore.Images.Media.DATA);
                String uriString = file.toString();
                Uri uri = Uri.parse(uriString);
                if (!myImageArray.contains(uri)) {
                    myImageArray.add(uri);
                    mAdapter.notifyDataSetChanged();
                }
                mainImageDisplay.setImageURI(uri);
            }
        }
    }
}