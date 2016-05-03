package com.moor.im.options.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.event.UnReadCount;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.views.ntb.NavigationTabBar;
import com.moor.im.options.contacts.fragment.ContactFragment;
import com.moor.im.options.message.fragment.MessageFragment;
import com.moor.im.options.setup.SetupFragment;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 主界面框架
 */
public class MainActivity extends AppCompatActivity{

    private ViewPager mViewPager;
    private List<Fragment> mTabsFragment = new ArrayList<Fragment>();
    private FragmentPagerAdapter mAdapter;
    private NavigationTabBar navigationTabBar;

    private Fragment fragment_message;
    private Fragment fragment_contact;
    private Fragment fragment_dial;
    private Fragment fragment_setup;



    SharedPreferences myPreferences;
    SharedPreferences.Editor editor;

    private CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPreferences = getSharedPreferences(MobileApplication.getInstance()
                        .getResources().getString(R.string.spname),
                Activity.MODE_PRIVATE);
        editor = myPreferences.edit();
        editor.putString("ClickState", "STATE_SHOW");
        editor.putString("moveState", "STATE_MOVE");
        editor.commit();

        mViewPager = (ViewPager) findViewById(R.id.id_main_viewpager);

        initDatas();

        _subscriptions = new CompositeSubscription();
        _subscriptions.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof UnReadCount) {
                            //接收到未读消息数
                            int unReadCount = ((UnReadCount) event).unReadCount;
                            if(unReadCount == 0) {
                                navigationTabBar.getModels().get(0).hideBadge();
                            }else {
                                navigationTabBar.getModels().get(0).setBadgeTitle(unReadCount+"");
                                navigationTabBar.getModels().get(0).showBadge();
                                navigationTabBar.postInvalidate();
                            }

                        }
                    }
                }));

    }
    /**
     * 初始化Fragment以及fragmentadapter
     */
    private void initDatas() {
        fragment_message = new MessageFragment();
        mTabsFragment.add(fragment_message);

        fragment_contact = new ContactFragment();
        mTabsFragment.add(fragment_contact);

        fragment_dial = new DialFragment();
        mTabsFragment.add(fragment_dial);

        fragment_setup = new SetupFragment();
        mTabsFragment.add(fragment_setup);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mTabsFragment.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mTabsFragment.get(arg0);
            }
        };
        mViewPager.setAdapter(mAdapter);

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_chat_black_48dp), Color.parseColor(colors[0]), "消息"));
        models.add(new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_contacts_black_48dp), Color.parseColor(colors[0]), "通讯录"));
        models.add(new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_dialpad_black_48dp), Color.parseColor(colors[0]), "电话"));
        models.add(new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_person_black_48dp), Color.parseColor(colors[0]), "我"));
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(mViewPager, 0);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {

            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _subscriptions.unsubscribe();
    }
}
