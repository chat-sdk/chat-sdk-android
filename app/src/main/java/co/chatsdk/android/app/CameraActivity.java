package co.chatsdk.android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {

    private boolean isRecording = false;
    private CameraKitView cameraKitView;
    File galleryFolder;
    File videoFolder;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_camera);
            cameraKitView = findViewById(R.id.camera);

            createImageGallery();
            createVideoGallery();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onTakeVideoButtonClicked(View v) {

        if (isRecording) {
            cameraKitView.stopVideo();
            isRecording = false;
            return;
        }
/*Here is where I tried to make the video recording work. As you can see I tried to make it work using the old `import com.wonderkiln.camerakit.CameraKitVideo;` import which
is from version 0.13.1. I still can'T make it work though. There appears to be no method for actually creating a video in the core camerakit.io files.
        cameraKitView.captureVideo(new CameraKitView.VideoCallback() {
            @Override
            public void onVideo(CameraKitView cameraKitView, Object o) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String videoFileName = "video_" + timeStamp + ".mp4";
                File savedVideo = new File(videoFolder, videoFileName);
                cameraKitView.startVideo();
                try {
                    FileOutputStream outputStream = new FileOutputStream(savedVideo.getPath());
                    outputStream.
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
        });
        isRecording = true;
        */
    }

    public void onTakePhotoButtonClicked(View v) {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "image_" + timeStamp + ".jpg";
                File savedPhoto = new File(galleryFolder, imageFileName);
                try {
                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                    outputStream.write(capturedImage);
                    outputStream.close();

                    displayMessage("Image Saved!");
                    goToImageEditing(savedPhoto);

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createImageGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        galleryFolder = new File(storageDirectory, getResources().getString(R.string.picture_folder));
        if (!galleryFolder.exists()) {
            boolean wasCreated = galleryFolder.mkdirs();
            if (!wasCreated) {
                Log.e("CapturedImages", "Failed to create image directory");
            }
        }
    }

    private void createVideoGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        videoFolder = new File(storageDirectory, getResources().getString(R.string.video_folder));
        if (!videoFolder.exists()) {
            boolean wasCreated = galleryFolder.mkdirs();
            if (!wasCreated) {
                Log.e("CapturedVideos", "Failed to create video directory");
            }
        }
    }

    private void displayMessage(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void goToImageEditing(File imageToEdit) {
        Intent i = new Intent(CameraActivity.this, ImageEditActivity.class);
        i.putExtra("imageToEdit", imageToEdit);
        startActivity(i);
    }

    public void switchCamera (View v) {
        cameraKitView.toggleFacing();
    }
}