package sdk.chat.demo;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.xmpp.fragments.XMPPConfigureFragment;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import sdk.chat.android.live.R;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class DemoActivity extends BaseActivity {

    protected DemoPagerAdapter adapter;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.pageIndicatorView)
    PageIndicatorView pageIndicatorView;

    @Override
    protected int getLayout() {
        return R.layout.activity_demo;
    }

    protected BackendFragment backendFragment = new BackendFragment();
    protected DatabaseFragment databaseFragment = new DatabaseFragment();
    protected FirebaseLoginTypeFragment firebaseLoginTypeFragment = new FirebaseLoginTypeFragment();
    protected LaunchFragment launchFragment = new LaunchFragment();
    protected StyleFragment styleFragment = new StyleFragment();
    protected WelcomeFragment welcomeFragment = new WelcomeFragment();
    protected XMPPServerFragment xmppServerFragment = new XMPPServerFragment();
    protected XMPPConfigureFragment xmppConfigureFragment = new XMPPConfigureFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new DemoPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.fragments.add(welcomeFragment);
        adapter.fragments.add(styleFragment);
        adapter.fragments.add(backendFragment);

        DemoConfigBuilder.shared().load(this);
        updateBackend(DemoConfigBuilder.shared().backend);

        adapter.notifyDataSetChanged();

        viewPager.setAdapter(adapter);

        dm.add(DemoConfigBuilder.shared().updated.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(s -> {
            Set<Fragment> fragmentSet = new HashSet<>(adapter.fragments);
            if (s == DemoConfigBuilder.Updated.Backend) {

                adapter.fragments.subList(3, adapter.fragments.size()).clear();

                DemoConfigBuilder.Backend backend = DemoConfigBuilder.shared().backend;

                if (backend != null) {
                    updateBackend(backend);
                    adapter.fragments.add(launchFragment);
                }
            }

            if (s == DemoConfigBuilder.Updated.Database && DemoConfigBuilder.shared().backend == DemoConfigBuilder.Backend.XMPP) {
                DemoConfigBuilder.Database database = DemoConfigBuilder.shared().database;
                if (database == DemoConfigBuilder.Database.Custom) {
                    // Add an extra step
                    if (!adapter.fragments.contains(xmppConfigureFragment)) {
                        adapter.fragments.add(adapter.fragments.size() - 1, xmppConfigureFragment);
                    }
                } else {
                    adapter.fragments.remove(xmppConfigureFragment);
                }
            }

            if (!fragmentSet.equals(new HashSet<>(adapter.fragments))) {
                viewPager.post(() -> {
                    adapter.notifyDataSetChanged();
                    pageIndicatorView.setCount(adapter.fragments.size());
                });
            }
        }));

//        DemoConfigBuilder.shared().updated.onNext(DemoConfigBuilder.Updated.Backend);

        pageIndicatorView.setSelectedColor(ContextCompat.getColor(this, R.color.chat_orange));
        pageIndicatorView.setUnselectedColor(ContextCompat.getColor(this, R.color.light_grey));
        pageIndicatorView.setAnimationType(AnimationType.DROP);
        pageIndicatorView.setInteractiveAnimation(true);
        pageIndicatorView.setRadius(5);
        pageIndicatorView.setDynamicCount(true);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageIndicatorView.setSelection(position);
                BaseFragment fragment = (BaseFragment) adapter.fragments.get(position);
                fragment.setTabVisibility(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    public void updateBackend(DemoConfigBuilder.Backend backend) {
        if (backend == DemoConfigBuilder.Backend.XMPP) {
            adapter.fragments.add(xmppServerFragment);
        } else {
            adapter.fragments.add(firebaseLoginTypeFragment);
            if(backend == DemoConfigBuilder.Backend.FireStream) {
                adapter.fragments.add(databaseFragment);
            }
        }
    }
}
