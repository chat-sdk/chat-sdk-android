package sdk.chat.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.xmpp.adapter.fragments.XMPPConfigureFragment;
import butterknife.BindView;
import io.reactivex.plugins.RxJavaPlugins;
import sdk.chat.ui.activities.BaseActivity;
import sdk.chat.ui.fragments.BaseFragment;
import sdk.guru.common.RX;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class DemoActivity extends BaseActivity {

    protected DemoPagerAdapter adapter;

    @BindView(R2.id.viewPager)
    ViewPager viewPager;

    @BindView(R2.id.pageIndicatorView)
    PageIndicatorView pageIndicatorView;

    BaseFragment currentFragment = null;

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
    protected InfoFragment infoFragment = new InfoFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxJavaPlugins.setErrorHandler(this);

        adapter = new DemoPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.add(welcomeFragment);
        adapter.add(infoFragment);
        adapter.add(styleFragment);

        // TODO: XX1 For the moment

//        adapter.add(firebaseLoginTypeFragment);
//        adapter.add(welcomeFragment);

        // TODO: XX1 For the moment, only allow the Firebase mode

        // adapter.add(backendFragment);

        viewPager.setAdapter(adapter);

        // TODO: XX1 For the moment, only allow the Firebase mode
        DemoConfigBuilder.shared().setBackend(DemoConfigBuilder.Backend.Firebase);
        DemoConfigBuilder.shared().setDatabase(DemoConfigBuilder.Database.Realtime);

        dm.add(DemoConfigBuilder.shared().updated.subscribeOn(RX.io()).observeOn(RX.io()).subscribe(s -> {
            Set<Fragment> fragmentSet = new HashSet<>(adapter.get());

            List<Fragment> fragments = new ArrayList<>(adapter.get());

            if ((s == DemoConfigBuilder.Updated.Backend || s == DemoConfigBuilder.Updated.All)) {

                //  TODO: XX1 For the moment, only allow the Firebase mode
                if (fragments.size() > 3) {
                    fragments.subList(4, fragments.size()).clear();
                }

                DemoConfigBuilder.Backend backend = DemoConfigBuilder.shared().backend;

                if (backend != null) {

                    if (backend == DemoConfigBuilder.Backend.XMPP) {
                        fragments.add(xmppServerFragment);
                    } else {
                        fragments.add(firebaseLoginTypeFragment);
                        if(backend == DemoConfigBuilder.Backend.FireStream) {
                            fragments.add(databaseFragment);
                        }
                    }

                    fragments.add(launchFragment);
                }
            }

            if ((s == DemoConfigBuilder.Updated.Database && DemoConfigBuilder.shared().backend == DemoConfigBuilder.Backend.XMPP) || s == DemoConfigBuilder.Updated.All) {
                DemoConfigBuilder.Database database = DemoConfigBuilder.shared().database;
                if (database == DemoConfigBuilder.Database.Custom) {
                    // Add an extra step
                    if (!fragments.contains(xmppConfigureFragment)) {
                        fragments.add(fragments.size() - 1, xmppConfigureFragment);
                    }
                } else {
                    fragments.remove(xmppConfigureFragment);
                }
            }

            if (!fragmentSet.equals(new HashSet<>(fragments))) {
                RX.main().scheduleDirect(() -> {
                    adapter.setFragments(fragments);
                    adapter.notifyDataSetChanged();
                    pageIndicatorView.setCount(adapter.getCount());
                });
            }
        }));

        DemoConfigBuilder.shared().load(this);
        DemoConfigBuilder.shared().updated.accept(DemoConfigBuilder.Updated.All);

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
                pageIndicatorView.setCount(adapter.getCount());
                pageIndicatorView.setSelection(position);
                BaseFragment fragment = (BaseFragment) adapter.get().get(position);
                fragment.setTabVisibility(true);
                if (currentFragment != null) {
                    currentFragment.setTabVisibility(false);
                }
                currentFragment = fragment;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
