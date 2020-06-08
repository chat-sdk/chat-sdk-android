package sdk.chat.ui.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.google.android.material.appbar.AppBarLayout;

public class ProfileViewOffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {


//    int avatarWidth;
//    int avatarHeight;

    float lastFraction;

    AlphaAnimation alphaAnimation;

    View view;

    public ProfileViewOffsetChangeListener(View view) {
        this.view = view;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        float fraction = Math.abs(verticalOffset / (float) appBarLayout.getTotalScrollRange());

//        float abHeight = appBarLayout.getMeasuredHeight();

        // If we are opening
//        if (lastFraction > fraction) {
//            if (fraction < 0.1 && alphaAnimation == null) {
//                alphaAnimation = new AlphaAnimation(avatarImageView.getAlpha(), 1f);
//                alphaAnimation.setDuration(200);
////                alphaAnimation.setFillAfter(true);
//                alphaAnimation.setInterpolator(new AccelerateInterpolator());
//                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        alphaAnimation = null;
//                        avatarImageView.setAlpha(1f);
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
//                avatarImageView.startAnimation(alphaAnimation);
//            }
//        } else {
            view.setAlpha(1.6f - fraction / 0.35f);
//        }

//        b.toolbar.setAlpha(fraction / 0.25f);

//        if (fraction < 0.25) {
//            b.avatarImageView.setAlpha(Math.round(1 - fraction / 0.25f));
//        } else {
//            b.avatarImageView.setAlpha(0);
//        }

//        b.avatarImageView.getLayoutParams().height = Math.round(avatarHeight * fraction);
//        b.avatarImageView.setScaleX(1 - 0.5f * fraction);
//        b.avatarImageView.setScaleY(1 - 0.5f * fraction);
//        b.avatarImageView.setTranslationX(-(1-fraction) * 300);
//        b.avatarImageView.setTranslationY((1-fraction) * 500);
//        b.avatarImageView.setScaleX(fraction);


//        CoordinatorLayout.LayoutParams params = getAvatarImageViewLayoutParams();
//        params.width = 700;
//        params.height = Math.round(avatarHeight * fraction);
//        params.width = Math.round(avatarWidth * fraction);
//        b.avatarImageView.setLayoutParams(params);


//        if (lastFraction < 0.5 && fraction >= 0.5) {
//            b.avatarImageView.startAnimation(scale(1, 0, () -> {
//                b.avatarImageView.setVisibility(View.INVISIBLE);
//            }));
//        }
//        if (lastFraction >= 0.5 && fraction < 0.5) {
//            b.avatarImageView.setVisibility(View.VISIBLE);
//            b.avatarImageView.startAnimation(scale(0, 1, () -> {
//                b.avatarImageView.clearAnimation();
//            }));
//        }

//        Logger.info(b.collapsingToolbar.getLayoutParams().height);
//        Logger.info(b.collapsingToolbar.getLayoutParams().width);

        lastFraction = fraction;
    }



//    public CoordinatorLayout.LayoutParams getAvatarImageViewLayoutParams() {
//        return (CoordinatorLayout.LayoutParams) b.avatarImageView.getLayoutParams();
//    }

//    public ScaleAnimation scale(float from, float to, Runnable onComplete) {
//        ScaleAnimation animation = new ScaleAnimation(
//                from, to,
//                from, to,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        animation.setDuration(200);
//        animation.setInterpolator(new AccelerateInterpolator(1.0f));
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                if (onComplete != null) {
//                    onComplete.run();
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        return animation;
//    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources resources = context.getResources();
        return resources.getDisplayMetrics();
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * (getDisplayMetrics(context).densityDpi / 160f);
    }

    public static int convertDpToPixelSize(float dp, Context context) {
        float pixels = convertDpToPixel(dp, context);
        final int res = (int) (pixels + 0.5f);
        if (res != 0) {
            return res;
        } else if (pixels == 0) {
            return 0;
        } else if (pixels > 0) {
            return 1;
        }
        return -1;
    }

}
