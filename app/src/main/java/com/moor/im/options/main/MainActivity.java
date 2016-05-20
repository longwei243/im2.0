package com.moor.im.options.main;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.utils.Log;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.MessageDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.db.dao.UserRoleDao;
import com.moor.im.common.event.DialEvent;
import com.moor.im.common.event.UnReadCount;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.ntb.NavigationTabBar;
import com.moor.im.options.contacts.activity.ContactsSearchActivity;
import com.moor.im.options.contacts.fragment.ContactFragment;
import com.moor.im.options.dial.DialFragment;
import com.moor.im.options.login.LoginActivity;
import com.moor.im.options.message.fragment.MessageFragment;
import com.moor.im.options.setup.SetupFragment;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 主界面框架
 */
public class MainActivity extends AppCompatActivity implements DialFragment.OnMakeCallListener{

    private ViewPager mViewPager;
    private List<Fragment> mTabsFragment = new ArrayList<Fragment>();
    private FragmentPagerAdapter mAdapter;
    private NavigationTabBar navigationTabBar;

    private ImageView title_btn_contact_search;

    private Fragment fragment_message;
    private Fragment fragment_contact;
    private Fragment fragment_dial;
    private Fragment fragment_setup;

    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            System.out.println("-----------执行了 ServiceConnection");
            service = ISipService.Stub.asInterface(arg1);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };

    SharedPreferences myPreferences;
    SharedPreferences.Editor editor;

    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    private CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileApplication.getInstance().add(this);
        myPreferences = getSharedPreferences(MobileApplication.getInstance()
                        .getResources().getString(R.string.spname),
                Activity.MODE_PRIVATE);
        editor = myPreferences.edit();
        editor.putString("ClickState", "STATE_SHOW");
        editor.putString("moveState", "STATE_MOVE");
        editor.commit();
        mSp = getSharedPreferences(M7Constant.MAIN_SP, 0);
        mEditor = mSp.edit();
        mViewPager = (ViewPager) findViewById(R.id.id_main_viewpager);
        title_btn_contact_search = (ImageView) findViewById(R.id.title_btn_contact_search);
        title_btn_contact_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactSearchIntent = new Intent(MainActivity.this, ContactsSearchActivity.class);
                startActivity(contactSearchIntent);
            }
        });

        checkPremission();

    }

    private void init() {
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

        //注册sip账户
        Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
        sendBroadcast(intent);
        bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
                , connection,
                Context.BIND_AUTO_CREATE);
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
        NavigationTabBar.Model model_msg = new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_chat_black_48dp), Color.parseColor(colors[0]), "消息");
        models.add(model_msg);
        NavigationTabBar.Model model_contact = new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_contacts_black_48dp), Color.parseColor(colors[0]), "通讯录");
        models.add(model_contact);
        NavigationTabBar.Model model_dial = new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_dialpad_black_48dp), Color.parseColor(colors[0]), "电话");
        models.add(model_dial);
        NavigationTabBar.Model model_my = new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_person_black_48dp), Color.parseColor(colors[0]), "我");
        models.add(model_my);
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(mViewPager, 0);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {

            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                if(index == 2) {
                    editor.putString("moveState", "STATE_CURRENT");
                    editor.commit();
                }else {
                    editor.putString("moveState", "STATE_MOVE");
                    editor.commit();
                }
            }
        });

        navigationTabBar.setListener(new NavigationTabBar.OnModelClickListener() {
            @Override
            public void onModelClickListener(int index) {

                if(index == 2) {
                    final String dia_key = myPreferences.getString("ClickState", "").trim();
                    final String dia_t1 = myPreferences.getString("moveState", "").trim();

                    if (dia_t1.equals("STATE_MOVE")) {
                        editor.putString("moveState", "STATE_CURRENT");
                        editor.commit();
                    } else {
                        if (dia_key.equals("") | dia_key.equals("STATE_SHOW")) {
                            editor.putString("ClickState", "STATE_HIDE");
                            editor.commit();
                            // 设置隐藏后的图标
                        } else {
                            editor.putString("ClickState", "STATE_SHOW");
                            editor.commit();
                        }
                    }
                    RxBus.getInstance().send(new DialEvent());
                }
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
        unbindService(connection);
    }

    @Override
    public void makeCall(String callee) {
        Long id = -1L;
        Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                id = c.getLong(c.getColumnIndex("id"));
            }
        }
        try {
            service.makeCall(callee, id.intValue());
        } catch (RemoteException e) {
            Toast.makeText(MainActivity.this, "拨打电话失败", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    private void checkPremission() {
        if(Build.VERSION.SDK_INT < 23) {
            init();
        }else {
            //6.0
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                //该权限已经有了
                init();
            }else {
                //申请该权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALL_LOG}, 0x1111);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0x1111:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                }else {

                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //修改过密码
        if("true".equals(InfoDao.getInstance().getIsChangePW())) {
            clearData();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void clearData() {
        //清空原来保存的数据
        mEditor.putBoolean(M7Constant.SP_LOGIN_SUCCEED ,false);
        mEditor.commit();
        getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);
        MessageDao.getInstance().deleteAllMsgs();
        NewMessageDao.getInstance().deleteAllMsgs();
        UserDao.getInstance().deleteUser();
        UserRoleDao.getInstance().deleteUserRole();
        MobileApplication.cacheUtil.clear();
    }
}
