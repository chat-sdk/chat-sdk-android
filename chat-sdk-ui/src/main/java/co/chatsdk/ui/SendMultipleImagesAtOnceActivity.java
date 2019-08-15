package co.chatsdk.ui;

import android.app.Activity;
import android.content.Context;
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
import java.util.function.Consumer;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.main.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static co.chatsdk.ui.chat.MediaSelector.CHOOSE_PHOTO;

public class SendMultipleImagesAtOnceActivity extends BaseActivity {

    private String imageURI;
    private RecyclerView multipleImageRecycler;
    public static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public static ArrayList<Uri> myImageArray = new ArrayList<>();
    //This crap has to be static or else it won't work in MyAdapter
    public static PhotoView mainImageDisplay;
    public static Uri mainImageUri;

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
        mainImageUri = uri;

        //Adapter here
        mAdapter = new MyAdapter(myImageArray);
        multipleImageRecycler.setAdapter(mAdapter);
        MyAdapter.addImageToMyAdapter(uri);
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
                for(int i = 0; i <= myImageArray.size(); i++) {
                    Uri uri = myImageArray.get(i);
                    File file = new File(uri.getPath());
                    Disposable d = ChatSDK.imageMessage().sendMessageWithImage(file, thread).subscribe(() -> {
                        // Handle Success
                    }, (Consumer<Throwable>) throwable -> {
                        // Handle failure
                    });
                }
            }
        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

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