package co.chatsdk.ui.UiHelpers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import co.chatsdk.core.utils.volley.VolleyUtils;

import co.chatsdk.ui.R;

/**
 * Created by kykrueger on 2017-02-19.
 */

public class ImageSetter {

    /** Load profile picture for given url and image view.*/
    public static void setPicture(final ImageView circleImageView, final String url, final boolean sender, final boolean isScrolling){

        if (url == null)
        {
            circleImageView.setImageResource(R.drawable.avatar_male_big);
            return;
        }

        circleImageView.setTag(url);

        VolleyUtils.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {

                // Checking to see that there is no new rewuest on this image.
                if (circleImageView.getTag() != null && !circleImageView.getTag().equals(url))
                    return;

                if (isImmediate && response.getBitmap() == null)
                {
                    circleImageView.setImageResource(R.drawable.avatar_male_big);
                    return;
                }

                if (response.getBitmap() != null)
                {
                    if (!isScrolling)
                    {
                        circleImageView.setImageBitmap(response.getBitmap());
                    }
                    else
                    {
                        animateSides(circleImageView, !sender, new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                circleImageView.setImageBitmap(response.getBitmap());
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        }, isScrolling);

                        circleImageView.getAnimation().start();
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                circleImageView.setImageResource(R.drawable.avatar_male_big);
            }
        }, circleImageView.getWidth(), circleImageView.getWidth());
    }

    /**
     * Animating the sides of the row, For example animating the user profile image and the message date.
     * */
    private static void animateSides(View view, boolean fromLeft, Animation.AnimationListener animationListener, boolean isScrolling){
        if (!isScrolling)
            return;

        if (fromLeft)
            view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.expand_slide_form_left));
        else view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.expand_slide_form_right));

        view.getAnimation().setAnimationListener(animationListener);
        view.animate();
    }
}
