package co.chatsdk.android.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import co.chatsdk.ui.main.BaseActivity;

public class ImageEditActivity extends BaseActivity {

    private File theImage;
    private String theImagePath;
    private Bitmap picBitmap;
    private Integer viewingTime;
    private Integer minutesInt;
    private Integer secondsInt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imageedit);
        Intent i = getIntent();
        theImage = (File) i.getSerializableExtra("imageToEdit");
        theImagePath = theImage.getPath();
        picBitmap = BitmapFactory.decodeFile(theImagePath);
        viewingTime = 0;
        secondsInt = 0;
        minutesInt = 0;
        setTheImage();

        Button selectViewingTime = (Button) findViewById(R.id.button2);
        Button selectRecipient = (Button) findViewById(R.id.button3);

        TextView minutesText = (TextView) findViewById(R.id.minutes_text);
        TextView secondsText = (TextView) findViewById(R.id.seconds_text);
        Spinner minutesSpinner = (Spinner) findViewById(R.id.minutes_spinner);
        Spinner secondsSpinner = (Spinner) findViewById(R.id.seconds_spinner);
        Button confirmViewingTimeButton = (Button) findViewById(R.id.confirm_viewing_time);
        Button noTimeLimitButton = (Button) findViewById(R.id.no_time_limit);

        ArrayList<Integer> numbers = new ArrayList<>();
        for (int ii = 0; ii < 60; ii++) {
            numbers.add(ii);
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numbers);
        minutesSpinner.setAdapter(adapter);
        secondsSpinner.setAdapter(adapter);

        minutesText.setVisibility(View.GONE);
        secondsText.setVisibility(View.GONE);
        minutesSpinner.setVisibility(View.GONE);
        secondsSpinner.setVisibility(View.GONE);
        confirmViewingTimeButton.setVisibility(View.GONE);
        noTimeLimitButton.setVisibility(View.GONE);

        selectViewingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minutesText.getVisibility() == View.VISIBLE && secondsText.getVisibility() == View.VISIBLE) {
                    minutesText.setVisibility(View.GONE);
                    secondsText.setVisibility(View.GONE);
                    minutesSpinner.setVisibility(View.GONE);
                    secondsSpinner.setVisibility(View.GONE);
                    confirmViewingTimeButton.setVisibility(View.GONE);
                    noTimeLimitButton.setVisibility(View.GONE);
                }
                else {
                    minutesText.setVisibility(View.VISIBLE);
                    secondsText.setVisibility(View.VISIBLE);
                    minutesSpinner.setVisibility(View.VISIBLE);
                    secondsSpinner.setVisibility(View.VISIBLE);
                    confirmViewingTimeButton.setVisibility(View.VISIBLE);
                    noTimeLimitButton.setVisibility(View.VISIBLE);
                }
            }
        });

        confirmViewingTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                minutesText.setVisibility(View.GONE);
                secondsText.setVisibility(View.GONE);
                minutesSpinner.setVisibility(View.GONE);
                secondsSpinner.setVisibility(View.GONE);
                confirmViewingTimeButton.setVisibility(View.GONE);
                noTimeLimitButton.setVisibility(View.GONE);
            }
        });

        noTimeLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewingTime = 0;
                minutesText.setVisibility(View.GONE);
                secondsText.setVisibility(View.GONE);
                minutesSpinner.setVisibility(View.GONE);
                secondsSpinner.setVisibility(View.GONE);
                confirmViewingTimeButton.setVisibility(View.GONE);
                noTimeLimitButton.setVisibility(View.GONE);
            }
        });

        minutesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                minutesInt = (Integer) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                minutesInt = 0;
            }
        });

        secondsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                secondsInt = (Integer) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                secondsInt = 0;
            }
        });
    }

    private void setTheImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        imageView.setImageBitmap(picBitmap);
    }

    public void onSendImageButtonClicked(View v) {
        Intent i = new Intent(ImageEditActivity.this, SelectSnapRecipientActivity.class);
        viewingTime = (minutesInt * 60) + secondsInt;
        i.putExtra("theImagePath", theImagePath);
        i.putExtra("viewing_time", viewingTime);
        startActivity(i);
    }
}

//https://github.com/leinardi/FloatingActionButtonSpeedDial
//branch alpha