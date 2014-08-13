package com.braunster.chatsdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.braunster.chatsdk.R;

/**
 * Created by braunster on 29/07/14.
 */
public class ChatBubbleTextView extends TextView {

    public static final String TAG = ChatBubbleTextView.class.getSimpleName();
    public static final boolean DEBUG = true;

    /** The size in pixels of the chat bubble point. i.e the the start of the bubble.*/
    private float pointSize = 6f * getResources().getDisplayMetrics().density;

    public static final int BubbleDefaultPressedColor = Color.parseColor("#27ae60");
    public static final int BubbleDefaultColor = Color.parseColor("#3498db");

    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_RIGHT = 1;

    private int bubbleGravity = GRAVITY_LEFT, bubbleColor = BubbleDefaultColor, pressedColor = BubbleDefaultPressedColor;

    public ChatBubbleTextView(Context context) {
        super(context);

        init();
    }

    public ChatBubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getAttrs(attrs);

        init();
    }

    public ChatBubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        getAttrs(attrs);

        init();
    }

    private void init(){
        if (DEBUG) Log.v(TAG, "init");
        setWillNotDraw(false);

        if (bubbleGravity == GRAVITY_LEFT)
            setPadding((int) (15 * getResources().getDisplayMetrics().density), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        else
            setPadding(getPaddingLeft(), getPaddingTop(), (int) (15 * getResources().getDisplayMetrics().density), getPaddingBottom());
    }

    private void getAttrs(AttributeSet attrs){
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.ChatBubbleTextView);

        try {
            // Gravity of the bubble. Left or Right.
            bubbleGravity = a.getInt(
                    R.styleable.ChatBubbleTextView_bubble_gravity, GRAVITY_LEFT);

            // Bubble color. The color could be changed when loading the the image url.
            bubbleColor = a.getColor(R.styleable.ChatBubbleTextView_bubble_color, BubbleDefaultColor);
        } finally {
            a.recycle();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (DEBUG) Log.v(TAG, "onMeasure W: " + getMeasuredWidth() + ", TextView  H: " + getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        if (DEBUG) Log.v(TAG, "onDraw");

        if (DEBUG) Log.d(TAG, "TextView W: " + getMeasuredWidth() + ", TextView  H: " + getMeasuredHeight());

        // setting the bubble color to the user message color.
        Bitmap bubble;
        if (bubbleGravity == GRAVITY_LEFT)
        {
            bubble = ChatBubbleImageView.get_ninepatch(R.drawable.bubble_left, (int) (getMeasuredWidth() - pointSize), getMeasuredHeight(), getContext());
            bubble = ChatBubbleImageView.setBubbleColor(bubble, bubbleColor);

            canvas.drawBitmap(bubble, pointSize, 0, null);
        }
        else
        {
            bubble = ChatBubbleImageView.get_ninepatch(R.drawable.bubble_right, (int) (getMeasuredWidth() - pointSize), getMeasuredHeight(), getContext());
            bubble = ChatBubbleImageView.setBubbleColor(bubble, bubbleColor);

            canvas.drawBitmap(bubble, 0, 0, null);
        }

        if (DEBUG) Log.d(TAG, "Bubble W: " + bubble.getWidth() + ", Bubble H: " + bubble.getHeight());



        super.onDraw(canvas);
//        int yPos = (int) ((canvas.getHeight() / 2) - ((getPaint().descent() + getPaint().ascent()) / 2)) ;
//
//        drawText(canvas, getText().toString(), 0 , getText().toString().length(), pointSize, yPos, getPaint());

        /*int index = 0;
        int lineCount = 0;
        List<String> lines = new ArrayList<String>();

        while (index < getText().toString().length())
        {
            int lastIndex = index;
            index += new TextPaint().breakText(getText().toString(), index, getText().toString().length(), true, getMeasuredWidth(), null);
            lines.add(getText().toString().substring(lastIndex, index));
            lineCount++;
        }
        if (lines.size() == 1)
        {
            drawText(canvas, lines.get(0), 0 , lines.get(0).length(), pointSize, yPos, getPaint());
            return;
        }

        for (String text : lines)
            drawText(canvas, text, 0 , text.length(), pointSize, yPos, getPaint());
*/


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (DEBUG) Log.v(TAG, "onLayout");
    }

    private void drawText(Canvas c, String text, int start, int end,
                         float x, float y, Paint p) {
        if (DEBUG) Log.v(TAG, "drawText, Text: " + text + ", Start: " + start  + ", End: " + end);
        c.drawText(text, start, end, x, y, p);
    }

    public void setBubbleColor(int bubbleColor) {
        this.bubbleColor = bubbleColor;
    }

    public void setBubbleGravity(int bubbleGravity) {
        this.bubbleGravity = bubbleGravity;
    }
}
