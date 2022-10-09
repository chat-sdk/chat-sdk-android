package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;

public class XMPPServerFragment extends CardViewFragment {

    CardView openFireCardView;
    CardView customServerCardView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_xmpp_server;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Not sure if these should be in initViews(), but since view is created here, why not.
        openFireCardView = view.findViewById(R.id.openFireCardView);
        customServerCardView = view.findViewById(R.id.customServerCardView);

        initViews();

        return view;
    }

    @Override
    protected void initViews() {
        super.initViews();

        setSelectionListener(openFireCardView);
        setSelectionListener(customServerCardView);

        if (selected == null) {
            DemoConfigBuilder.Database database = DemoConfigBuilder.shared().getDatabase();
            if (database != null) {
                switch (database) {
                    case Custom:
                        super.selectView(customServerCardView);
                        break;
                    case OpenFire:
                    default:
                        super.selectView(openFireCardView);
                }
            } else {
                selectView(openFireCardView);
            }
        } else {
            super.selectView(selected);
        }
    }

    @Override
    public Completable selectView(CardView view) {
        return selectView(view, 400);
    }

    @Override
    public Completable selectView(CardView view, long duration) {
        dm.add(super.selectView(view, duration).subscribe(() -> {
            if (view.equals(openFireCardView)) {
                DemoConfigBuilder.shared().setDatabase(DemoConfigBuilder.Database.OpenFire);
            }
            if (view.equals(customServerCardView)) {
                DemoConfigBuilder.shared().setDatabase(DemoConfigBuilder.Database.Custom);
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
