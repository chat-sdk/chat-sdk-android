package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;

public class FirebaseLoginTypeFragment extends CardViewFragment {

    @BindView(R2.id.firebaseUICardView)
    CardView firebaseUICardView;
    @BindView(R2.id.customLoginCardView)
    CardView customLoginCardView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_firebase_login_type;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();

        return view;
    }

    @Override
    protected void initViews() {
        super.initViews();

        setSelectionListener(firebaseUICardView);
        setSelectionListener(customLoginCardView);

        if (selected == null) {
            DemoConfigBuilder.LoginStyle style = DemoConfigBuilder.shared().getLoginStyle();
            if (style != null) {
                switch (style) {
                    case Custom:
                        super.selectView(customLoginCardView);
                        break;
                    case FirebaseUI:
                        super.selectView(firebaseUICardView);
                        break;
                }
            } else {
                selectView(firebaseUICardView);
            }
        } else {
            super.selectView(selected, 0);
        }
    }

//    @Override
//    public void setTabVisibility(boolean isVisible) {
//        if (isVisible) {
//
//        }
//    }

    @Override
    public Completable selectView(CardView view) {
        return selectView(view, 400);
    }

    @Override
    public Completable selectView(CardView view, long duration) {
        dm.add(super.selectView(view, duration).subscribe(() -> {
            if (view.equals(firebaseUICardView)) {
                DemoConfigBuilder.shared().setLoginStyle(DemoConfigBuilder.LoginStyle.FirebaseUI);
            }
            if (view.equals(customLoginCardView)) {
                DemoConfigBuilder.shared().setLoginStyle(DemoConfigBuilder.LoginStyle.Custom);
            }
        }, throwable -> {

        }));
        return Completable.complete();
    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }
}
