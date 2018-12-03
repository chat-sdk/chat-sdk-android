package co.chatsdk.profile.pictures;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import androidx.annotation.LayoutRes;
import co.chatsdk.ui.main.BaseActivity;

public class ImagePreviewActivity extends BaseActivity {

    protected Animator currentAnimator;
    protected int shortAnimationDuration;

    protected ViewGroup mainView;
    protected View backgroundView;
    protected SimpleDraweeView expandedImageView;

    protected View currentThumbnailView;
    protected float currentStartScale;
    protected Rect currentStartBounds;

    protected boolean minimizeOnClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activityLayout());
        mainView = findViewById(android.R.id.content);

        initViews();

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    protected @LayoutRes
    int activityLayout() {
        return 0;
    }

    protected void initViews() {
        addBackgroundView();
        addExpandedImageView();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void addBackgroundView() {
        backgroundView = new View(this);
        backgroundView.setBackgroundColor(Color.BLACK);
        backgroundView.setAlpha(0);
        mainView.addView(backgroundView);
        ViewGroup.LayoutParams params = backgroundView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        backgroundView.setLayoutParams(params);
    }

    protected void addExpandedImageView() {
        expandedImageView = new SimpleDraweeView(this);
        expandedImageView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
        expandedImageView.setVisibility(View.INVISIBLE);
        backgroundView.setClickable(false);
        mainView.addView(expandedImageView);
        ViewGroup.LayoutParams params = expandedImageView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        expandedImageView.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        if (isBackgroundVisible()) {
            minimizeToThumbnail(currentThumbnailView, currentStartBounds, currentStartScale);
        } else {
            super.onBackPressed();
        }
    }

    protected void updateBackgroundViewOnTouchListener() {
        backgroundView.setOnTouchListener((view, event) -> {
            if (!isBackgroundVisible()) {
                return view.performClick();
            }
            return true;
        });
    }

    protected boolean isBackgroundVisible() {
        return backgroundView.getAlpha() > 0;
    }

    protected void zoomImageFromThumbnail(final View thumbnailView, int imageResId) {
        expandedImageView.setImageResource(imageResId);
        zoomImageFromThumbnail(thumbnailView);
    }

    protected void zoomImageFromThumbnail(final View thumbnailView, String uri) {
        expandedImageView.setImageURI(uri);
        zoomImageFromThumbnail(thumbnailView);
    }

    protected void zoomImageFromThumbnail(final View thumbnailView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        backgroundView.animate().alpha(1);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbnailView.getGlobalVisibleRect(startBounds);
        findViewById(android.R.id.content).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbnailView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
                updateBackgroundViewOnTouchListener();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
                updateBackgroundViewOnTouchListener();
            }
        });
        set.start();
        currentAnimator = set;
        currentThumbnailView = thumbnailView;
        currentStartBounds = startBounds;
        currentStartScale = startScale;

        expandedImageView.setOnClickListener(view -> {
            if (minimizeOnClick) {
                minimizeToThumbnail(thumbnailView, startBounds, startScale);
            }
        });
    }

    protected void minimizeToThumbnail(final View thumbnailView, final Rect startBounds, final float startScale) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        backgroundView.animate().alpha(0);

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set1 = new AnimatorSet();
        set1.play(ObjectAnimator
                .ofFloat(expandedImageView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_X, startScale))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_Y, startScale));
        set1.setDuration(shortAnimationDuration);
        set1.setInterpolator(new DecelerateInterpolator());
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumbnailView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                currentAnimator = null;
                updateBackgroundViewOnTouchListener();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                thumbnailView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                currentAnimator = null;
                updateBackgroundViewOnTouchListener();
            }
        });
        set1.start();
        currentAnimator = set1;
    }

}
