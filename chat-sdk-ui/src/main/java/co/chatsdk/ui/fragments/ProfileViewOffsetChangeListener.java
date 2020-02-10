package co.chatsdk.ui.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

import org.pmw.tinylog.Logger;

import co.chatsdk.ui.databinding.FragmentProfileBinding;
import io.reactivex.functions.Action;

public class ProfileViewOffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {

    FragmentProfileBinding b;

    int avatarWidth;
    int avatarHeight;

    float lastFraction;

    public ProfileViewOffsetChangeListener(FragmentProfileBinding b) {
        this.b = b;
        avatarHeight = b.avatarImageView2.getLayoutParams().height;
        avatarWidth = b.avatarImageView2.getLayoutParams().width;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        float fraction = Math.abs(verticalOffset / (float) appBarLayout.getTotalScrollRange());

//        float abHeight = appBarLayout.getMeasuredHeight();

        b.avatarImageView2.setAlpha(1.6f - fraction / 0.35f);
//        b.toolbar.setAlpha(fraction / 0.25f);

//        if (fraction < 0.25) {
//            b.avatarImageView2.setAlpha(Math.round(1 - fraction / 0.25f));
//        } else {
//            b.avatarImageView2.setAlpha(0);
//        }

//        b.avatarImageView2.getLayoutParams().height = Math.round(avatarHeight * fraction);
//        b.avatarImageView2.setScaleX(1 - 0.5f * fraction);
//        b.avatarImageView2.setScaleY(1 - 0.5f * fraction);
//        b.avatarImageView2.setTranslationX(-(1-fraction) * 300);
//        b.avatarImageView2.setTranslationY((1-fraction) * 500);
//        b.avatarImageView2.setScaleX(fraction);


//        CoordinatorLayout.LayoutParams params = getAvatarImageViewLayoutParams();
//        params.width = 700;
//        params.height = Math.round(avatarHeight * fraction);
//        params.width = Math.round(avatarWidth * fraction);
//        b.avatarImageView2.setLayoutParams(params);


//        if (lastFraction < 0.5 && fraction >= 0.5) {
//            b.avatarImageView2.startAnimation(scale(1, 0, () -> {
//                b.avatarImageView2.setVisibility(View.INVISIBLE);
//            }));
//        }
//        if (lastFraction >= 0.5 && fraction < 0.5) {
//            b.avatarImageView2.setVisibility(View.VISIBLE);
//            b.avatarImageView2.startAnimation(scale(0, 1, () -> {
//                b.avatarImageView2.clearAnimation();
//            }));
//        }

//        Logger.info(b.collapsingToolbar.getLayoutParams().height);
//        Logger.info(b.collapsingToolbar.getLayoutParams().width);

        lastFraction = fraction;
    }



//    public CoordinatorLayout.LayoutParams getAvatarImageViewLayoutParams() {
//        return (CoordinatorLayout.LayoutParams) b.avatarImageView2.getLayoutParams();
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
