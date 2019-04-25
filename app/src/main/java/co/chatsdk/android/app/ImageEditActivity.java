package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class ImageEditActivity extends AppCompatActivity {

    private File theImage;
    private String theImagePath;
    private Bitmap picBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageedit);
        Intent i = getIntent();
        theImage = (File) i.getSerializableExtra("imageToEdit");
        theImagePath = theImage.getPath();
        picBitmap = BitmapFactory.decodeFile(theImagePath);
        setTheImage();
    }

    private void setTheImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        imageView.setImageBitmap(picBitmap);
    }

    public void onSendImageButtonClicked(View v) {
        Intent i = new Intent(ImageEditActivity.this, SelectSnapRecipientActivity.class);
        i.putExtra("theImagePath", theImagePath);
        startActivity(i);

    }
}
