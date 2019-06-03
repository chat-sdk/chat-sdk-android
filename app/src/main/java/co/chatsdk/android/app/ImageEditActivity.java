package co.chatsdk.android.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.graphics.Matrix;
import android.widget.TextView;

import androidx.renderscript.RenderScript;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.ArrayList;

import co.chatsdk.ui.main.BaseActivity;

public class ImageEditActivity extends BaseActivity {

    private File theImage;
    private String theImagePath;
    private Bitmap picBitmap;
    public Integer secondsInt;
    private Bitmap blurredBitmap;
    private Bitmap blurredBitmap2;
    private Bitmap lightenedBitmap;
    public ArrayList<Integer> timesInSeconds;
    public TextView timerText;
    public String[] timeOptionsStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        setContentView(R.layout.activity_imageedit);
        Intent i = getIntent();
        theImage = (File) i.getSerializableExtra("imageToEdit");
        theImagePath = theImage.getPath();
        picBitmap = BitmapFactory.decodeFile(theImagePath);
        timerText = (TextView) findViewById(R.id.timerText);
        setTheImage(picBitmap);
        secondsInt = 0;
        timerText.setText("No Time Limit");


        FloatingActionButton selectRecipient = (FloatingActionButton) findViewById(R.id.button3);
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.number_picker);
        numberPicker.setVisibility(View.GONE);
        //This doesn't have any effect.
        /*Drawable add = getResources().getDrawable(R.drawable.ic_add_white_24dp);
        add.setAlpha(255);*/

        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.setExpansionMode(SpeedDialView.ExpansionMode.BOTTOM);
        speedDialView.setMainFabClosedBackgroundColor(getResources().getColor(android.R.color.white));
        speedDialView.setMainFabOpenedBackgroundColor(getResources().getColor(android.R.color.white));
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_action1, R.drawable.ic_timer_white_24dp)
                        .setFabBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark))
                        .create()
        );

        ArrayList<Integer> timesInSeconds = new ArrayList<Integer>();
        for (int j = 0; j <= 10800; j++) {
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

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.fab_action1:

                        if (numberPicker.getVisibility() == View.VISIBLE) {
                            Integer value = numberPicker.getValue();
                            secondsInt = timesInSeconds.get(value);
                            numberPicker.setVisibility(View.GONE);
                            setTheImage(picBitmap);

                            Integer timeInSecondsIndex = timesInSeconds.indexOf(secondsInt);
                            String viewingTime = timeOptionsStrings[timeInSecondsIndex];
                            timerText.setText(viewingTime);
                        } else {
                            blurredBitmap = Blur.fastblur(context, picBitmap, 25);
                            blurredBitmap2 = Blur.fastblur(context, blurredBitmap, 25);
                            lightenedBitmap = lightenBitMap(blurredBitmap2);
                            setTheImage(lightenedBitmap);
                            numberPicker.setVisibility(View.VISIBLE);
                        }

                        showToast("Link action clicked!");
                        return true; // true to keep the Speed Dial open
                    default:
                        return true;
                }
            }
        });

        timeOptionsStrings = new String[timesInSeconds.size()];
        ArrayList<String> stringList = convertSecondsToText(timesInSeconds);
        for (Integer j = 0; j < timesInSeconds.size(); j++) {
            timeOptionsStrings[j] = stringList.get(j);
        }
        numberPicker.setMaxValue(timeOptionsStrings.length - 1);
        numberPicker.setDisplayedValues(timeOptionsStrings);
        numberPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTheImage(picBitmap);
                Integer value = numberPicker.getValue();
                secondsInt = timesInSeconds.get(value);
                numberPicker.setVisibility(View.GONE);

                Integer timeInSecondsIndex = timesInSeconds.indexOf(secondsInt);
                String viewingTime = timeOptionsStrings[timeInSecondsIndex];
                timerText.setText(viewingTime);
            }
        });
    }

// End of the onCreate method.

    private void setTheImage (Bitmap bitmap) {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        imageView.setImageBitmap(bitmap);
    }

    public void onSendImageButtonClicked (View v) {
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

    private Bitmap lightenBitMap(Bitmap bm) {

        Canvas canvas = new Canvas(bm);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222); // lighten
        //ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return bm;
    }
}