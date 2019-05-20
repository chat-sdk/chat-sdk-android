package co.chatsdk.android.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;

import androidx.renderscript.RenderScript;

import java.io.File;
import java.util.ArrayList;

import co.chatsdk.ui.main.BaseActivity;

public class ImageEditActivity extends BaseActivity {

    private File theImage;
    private String theImagePath;
    private Bitmap picBitmap;
    private Integer secondsInt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imageedit);
        Intent i = getIntent();
        theImage = (File) i.getSerializableExtra("imageToEdit");
        theImagePath = theImage.getPath();
        picBitmap = BitmapFactory.decodeFile(theImagePath);
        secondsInt = 0;
        setTheImage();

        Button selectViewingTime = (Button) findViewById(R.id.button2);
        Button selectRecipient = (Button) findViewById(R.id.button3);
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.number_picker);
        numberPicker.setVisibility(View.GONE);

        ImageView imgBgBlur = (ImageView) findViewById(R.id.imgBgBlur);
        imgBgBlur.setVisibility(View.GONE);

        ArrayList<Integer> timesInSeconds = new ArrayList<Integer>();
        for (int j = 0; j <=10800; j++) {
            if (j <= 10) {
                timesInSeconds.add(j);
            } else if (j == 15 || j == 30) {
                timesInSeconds.add(j);
            } else if (j >= 60 && j <= 300) {
                if (j == 60 || j == 120 || j == 180 || j == 240 || j == 300) {
                    timesInSeconds.add(j);
                }
            } else {
                if (j == 3600 || j == 7200 || j == 10800) {
                    timesInSeconds.add(j);
                }
            }
        }

        String[] timeOptionsStrings = new String[timesInSeconds.size()];
        ArrayList<String> stringList = convertSecondsToText(timesInSeconds);
        for (Integer j = 0; j < timesInSeconds.size(); j++) {
            timeOptionsStrings[j] = stringList.get(j);
        }
            numberPicker.setMaxValue(timeOptionsStrings.length - 1);
            numberPicker.setDisplayedValues(timeOptionsStrings);

            numberPicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer value = numberPicker.getValue();
                    secondsInt = timesInSeconds.get(value);
                    numberPicker.setVisibility(View.GONE);
                    imgBgBlur.setVisibility(View.GONE);
                }
            });

            selectViewingTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bitmap mBlurBitmap = createBlurBitmap();
                    imgBgBlur.setImageBitmap(mBlurBitmap);

                    if (imgBgBlur.getVisibility() == View.VISIBLE) {
                        imgBgBlur.setVisibility(View.GONE);
                    } else {
                        imgBgBlur.setVisibility(View.VISIBLE);
                    }

                    if (numberPicker.getVisibility() == View.VISIBLE) {
                        numberPicker.setVisibility(View.GONE);
                    } else {
                        numberPicker.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    public Bitmap createBlurBitmap() {
        View viewContainer = findViewById(R.id.imageDisplay);
        Bitmap bitmap = captureView(viewContainer);
        if (bitmap != null) {
            RenderScript rs = RenderScript.create(this);
            BlurHelper.blurBitmapWithRenderscript(
                    rs,
                    bitmap);
        }
        return bitmap;
    }

    public Bitmap captureView(View view) {
        //Create a Bitmap with the same dimensions as the View
        Bitmap image = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_4444); //reduce quality
        //Draw the view inside the Bitmap
        Canvas canvas = new Canvas(image);
        view.draw(canvas);

        //Make it frosty
        Paint paint = new Paint();
        paint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        ColorFilter filter =
                new LightingColorFilter(0xFFFFFFFF, 0x00222222); // lighten
        //ColorFilter filter =
        //   new LightingColorFilter(0xFF7F7F7F, 0x00000000); // darken
        paint.setColorFilter(filter);
        canvas.drawBitmap(image, 0, 0, paint);
        return image;
    }

    private void setTheImage () {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        imageView.setImageBitmap(picBitmap);
    }

    public void onSendImageButtonClicked (View v){
        Intent i = new Intent(ImageEditActivity.this, SelectSnapRecipientActivity.class);
        i.putExtra("theImagePath", theImagePath);
        i.putExtra("viewing_time", secondsInt);
        startActivity(i);
    }

    public ArrayList<String> convertSecondsToText (ArrayList<Integer> timeInSeconds) {
        ArrayList<String> stringList = new ArrayList<String>();
        for (Integer i = 0; i < timeInSeconds.size(); i++) {
            Integer secondsInt = timeInSeconds.get(i);
            if (secondsInt == 0) {
                stringList.add(i, "No Time Limit");
            } else if (secondsInt == 1) {
                stringList.add(i, "1 Second");
            } else if (secondsInt > 1 && secondsInt < 60) {
                stringList.add(i, Integer.toString(secondsInt) + " Seconds");
            } else if (secondsInt == 60) {
                stringList.add(i, "1 Minute");
            } else if (secondsInt > 60 && secondsInt < 3600) {
                stringList.add(i, Integer.toString(secondsInt / 60) + " Minutes");
            } else if (secondsInt == 3600) {
                stringList.add(i, "1 Hour");
            } else if (secondsInt > 3600) {
                stringList.add(i, Integer.toString(secondsInt / 3600) + " Hours");
            }
        }
        return stringList;
    }
}