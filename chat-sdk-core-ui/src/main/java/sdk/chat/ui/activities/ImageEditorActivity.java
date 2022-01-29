package sdk.chat.ui.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import butterknife.BindView;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import sdk.chat.core.dao.Keys;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;

public class ImageEditorActivity extends BaseActivity {

    public static int activityIdentifier = 957;

    @BindView(R2.id.photoEditorView) protected PhotoEditorView editorView;

    @Override
    protected int getLayout() {
        return R.layout.activity_image_editor;
    }

    PhotoEditor mPhotoEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        String imagePath = extras.getString(Keys.IntentKeyImagePath);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);

        editorView.getSource().setImageBitmap(bmp);

        //Use custom font using latest support library
        Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);

        //loading font from asset
        Typeface mEmojiTypeFace = ResourcesCompat.getFont(this, R.font.emojione_android);

        mPhotoEditor = new PhotoEditor.Builder(this, editorView)
                .setPinchTextScalable(true)
                .setClipSourceImage(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                .setDefaultEmojiTypeface(mEmojiTypeFace)
                .build();
    }
}
