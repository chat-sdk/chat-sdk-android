package sdk.chat.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Build;

import androidx.cardview.widget.CardView;

import sdk.guru.common.DisposableMap;
import sdk.chat.ui.fragments.BaseFragment;
import io.reactivex.Completable;

public abstract class CardViewFragment extends BaseFragment {

    protected CardView selected;
    protected DisposableMap dm = new DisposableMap();

    public void setSelectionListener(CardView view) {
        view.setOnClickListener(v -> {
            selectView(view);
        });
    }

    @Override
    protected void initViews() {
        selected = null;
    }


    public Completable selectView(CardView view) {
        return selectView(view, 200);
    }

    public Completable selectView(CardView view, long duration) {
        return Completable.create(emitter -> {
//            long dur = duration;
            long dur = 0;
            if (view.equals(selected)) {
                emitter.onError(new Throwable());
            }
            else {
                deselectView(dur);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "elevation", view.getElevation(), 25);
                    animator.setDuration(dur);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            selected = view;
                            emitter.onComplete();
                        }
                    });
                    animator.start();
                }
                view.setBackground(getResources().getDrawable(R.drawable.card_view_selected_shape));
            }
        });
    }

    public void deselectView(long duration) {
        if (selected != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(selected, "elevation", selected.getElevation(), 5);
                animator.setDuration(duration);
                animator.start();
            }
            selected.setBackground(getResources().getDrawable(R.drawable.card_view_shape));
        }
    }

}
