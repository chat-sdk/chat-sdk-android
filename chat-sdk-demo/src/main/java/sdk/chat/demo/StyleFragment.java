package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;

public class StyleFragment extends CardViewFragment {

    @BindView(R2.id.drawerCardView)
    CardView drawerCardView;
    @BindView(R2.id.tabsCardView)
    CardView tabsCardView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_style;
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

        setSelectionListener(drawerCardView);
        setSelectionListener(tabsCardView);

        if (selected == null) {
            DemoConfigBuilder.Style style = DemoConfigBuilder.shared().getStyle();
            if (style != null) {
                switch (style) {
                    case Tabs:
                        super.selectView(tabsCardView);
                        break;
                    case Drawer:
                        super.selectView(drawerCardView);
                        break;
                }
            } else {
                selectView(drawerCardView);
            }
        } else {
            super.selectView(selected);
        }
    }

//    @Override
//    public void setTabVisibility(boolean isVisible) {
//        if (isVisible) {
//
//    }

    @Override
    public Completable selectView(CardView view) {
        return selectView(view, 400);
    }

    @Override
    public Completable selectView(CardView view, long duration) {
        dm.add(super.selectView(view, duration).subscribe(() -> {
            if (view.equals(drawerCardView)) {
                DemoConfigBuilder.shared().setStyle(DemoConfigBuilder.Style.Drawer);
            }
            if (view.equals(tabsCardView)) {
                DemoConfigBuilder.shared().setStyle(DemoConfigBuilder.Style.Tabs);
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
