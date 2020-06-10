package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;

public class DatabaseFragment extends CardViewFragment {
    @BindView(R2.id.firestoreCardView)
    CardView firestoreCardView;
    @BindView(R2.id.realtimeCardView)
    CardView realtimeCardView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_database;
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

        setSelectionListener(firestoreCardView);
        setSelectionListener(realtimeCardView);

        if (selected == null) {
            DemoConfigBuilder.Database database = DemoConfigBuilder.shared().getDatabase();
            if (database != null) {
                switch (database) {
                    case Realtime:
                        super.selectView(realtimeCardView);
                        break;
                    case Firestore:
                    default:
                        super.selectView(firestoreCardView);
                }
            } else {
                selectView(firestoreCardView);
            }
        } else {
            super.selectView(selected, 0);
        }

    }

//    @Override
//    public void setTabVisibility(boolean isVisible) {
//        if (isVisible) {
//        }
//    }

    @Override
    public Completable selectView(CardView view) {
        return selectView(view, 400);
    }

    @Override
    public Completable selectView(CardView view, long duration) {
        dm.add(super.selectView(view, duration).subscribe(() -> {
            if (view.equals(firestoreCardView)) {
                DemoConfigBuilder.shared().setDatabase(DemoConfigBuilder.Database.Firestore);
            }
            if (view.equals(realtimeCardView)) {
                DemoConfigBuilder.shared().setDatabase(DemoConfigBuilder.Database.Realtime);
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
