package com.moor.im.options.setup;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csipsimple.api.SipProfile;
import com.m7.imkfsdk.chat.ChatActivity;
import com.m7.imkfsdk.chat.PeerDialog;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.MessageDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.db.dao.UserRoleDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.dialog.MaterialDialog;
import com.moor.im.common.event.UserInfoUpdate;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.model.UserRole;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.aboutme.AboutMeActivity;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.login.LoginActivity;
import com.moor.im.options.mobileassistant.cdr.activity.CdrActivity;
import com.moor.im.options.mobileassistant.erp.activity.ErpActivity;
import com.moor.im.options.setting.SettingActivity;
import com.moor.im.options.setup.activity.ClipImageViewActivity;
import com.moor.im.options.setup.activity.EditActivity;
import com.moor.im.options.update.UpdateActivity;
import com.moor.im.tcp.imservice.IMService;
import com.moor.imkf.GetPeersListener;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.InitListener;
import com.moor.imkf.model.entity.Peer;

import java.io.Serializable;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by longwei on 2016/3/16.
 */
public class SetupFragment extends BaseLazyFragment{
    RelativeLayout setup_ll_loginoff, setup_ll_update,
            setup_ll_icon,
            setup_ll_edit_name, setup_ll_edit_email,
            setup_ll_edit_phone, setup_ll_kefu, setup_ll_setting;

    TextView user_detail_tv_name, user_detail_tv_num, user_detail_tv_email, user_detail_tv_phone;

    ImageView contact_detail_image;
    private LoadingDialog loadingDialog = new LoadingDialog();
    private User user = UserDao.getInstance().getUser();
    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;
    private CompositeSubscription _subscriptions;
    private Messenger messenger;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName cn) {
        }

        @Override
        public void onServiceConnected(ComponentName cn, IBinder ibiner) {
            messenger = new Messenger(ibiner);//得到Service中的Messenger
            LogUtil.d("bind imservice success");
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, null);
        setup_ll_loginoff = (RelativeLayout) view.findViewById(R.id.setup_ll_loginoff);
        setup_ll_loginoff.setOnClickListener(clickListener);

        setup_ll_update = (RelativeLayout) view.findViewById(R.id.setup_ll_update);
        setup_ll_update.setOnClickListener(clickListener);

        setup_ll_icon = (RelativeLayout) view.findViewById(R.id.setup_ll_icon);
        setup_ll_icon.setOnClickListener(clickListener);

        setup_ll_edit_name = (RelativeLayout) view.findViewById(R.id.setup_ll_edit_name);
        setup_ll_edit_name.setOnClickListener(clickListener);

        setup_ll_edit_phone = (RelativeLayout) view.findViewById(R.id.setup_ll_edit_phone);
        setup_ll_edit_phone.setOnClickListener(clickListener);

        setup_ll_edit_email = (RelativeLayout) view.findViewById(R.id.setup_ll_edit_email);
        setup_ll_edit_email.setOnClickListener(clickListener);

        user_detail_tv_name = (TextView) view.findViewById(R.id.user_detail_tv_name);
        user_detail_tv_num = (TextView) view.findViewById(R.id.user_detail_tv_num);
        user_detail_tv_email = (TextView) view.findViewById(R.id.user_detail_tv_email);
        user_detail_tv_phone = (TextView) view.findViewById(R.id.user_detail_tv_phone);
        user_detail_tv_name.setText(user.displayName);
        user_detail_tv_num.setText(user.exten);
        user_detail_tv_email.setText(user.email);
        user_detail_tv_phone.setText(user.mobile);

        setup_ll_setting = (RelativeLayout) view.findViewById(R.id.setup_ll_setting);
        setup_ll_setting.setOnClickListener(clickListener);
        contact_detail_image = (ImageView) view.findViewById(R.id.user_icon);

        if(user.im_icon != null && !"".equals(user.im_icon)) {
            GlideUtils.displayNet(contact_detail_image, user.im_icon+M7Constant.QINIU_IMG_ICON);
        }else {
            GlideUtils.displayNative(contact_detail_image, R.drawable.img_default_head);
        }

        setup_ll_kefu = (RelativeLayout) view.findViewById(R.id.setup_ll_kefu);
        setup_ll_kefu.setOnClickListener(clickListener);

        _subscriptions = new CompositeSubscription();
        _subscriptions.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof UserInfoUpdate) {
                            //用户信息更新了
                            HttpManager.getInstance().getUserInfo(InfoDao.getInstance().getConnectionId(), new GetUserInfoResponseHandler());
                        }
                    }
                }));
        mSp = getActivity().getSharedPreferences(M7Constant.MAIN_SP, 0);
        mEditor = mSp.edit();
        getActivity().bindService(new Intent(getActivity(), IMService.class), conn, Context.BIND_AUTO_CREATE);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x1234 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                String realPath = getRealPathFromURI(uri);
                Intent intent = new Intent(getActivity(), ClipImageViewActivity.class);
                intent.putExtra("imagePath", realPath);
                startActivity(intent);
            }
        }
    }

    // 获取字符
    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null,
                    null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.setup_ll_loginoff:
                    showLoginoffDialog();
                    break;
                case R.id.setup_ll_update:
                    Intent updateIntent = new Intent(getActivity(), UpdateActivity.class);
                    startActivity(updateIntent);
                    break;
                case R.id.setup_ll_icon:
                    Intent intent;
                    if (Build.VERSION.SDK_INT < 19) {
                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                    } else {
                        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    }
                    startActivityForResult(intent, 0x1234);
                    break;
                case R.id.setup_ll_edit_name:
                    Intent editIntent = new Intent(getActivity(), EditActivity.class);
                    editIntent.putExtra("edittype", "name");
                    startActivity(editIntent);
                    break;
                case R.id.setup_ll_edit_phone:
                    Intent phoneIntent = new Intent(getActivity(), EditActivity.class);
                    phoneIntent.putExtra("edittype", "phone");
                    startActivity(phoneIntent);
                    break;
                case R.id.setup_ll_edit_email:
                    Intent emailIntent = new Intent(getActivity(), EditActivity.class);
                    emailIntent.putExtra("edittype", "email");
                    startActivity(emailIntent);
                    break;
                case R.id.setup_ll_kefu:
                    if(Build.VERSION.SDK_INT < 23) {
                        initKeFu();
                    }else {
                        //6.0
                        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            //该权限已经有了
                            initKeFu();
                        }else {
                            //申请该权限
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 0x1111);
                        }
                    }
                    break;
                case R.id.setup_ll_setting:
                    Intent settingIntent = new Intent(getActivity(), SettingActivity.class);
                    startActivity(settingIntent);
                    break;
            }
        }
    };

    /**
     * 注销对话框
     */
    private void showLoginoffDialog() {
        final MaterialDialog mMaterialDialog = new MaterialDialog(getActivity());
        mMaterialDialog.setTitle("温馨提示")
                .setMessage("确认注销吗?")
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        loginoff();
                    }
                })
                .setNegativeButton("取消",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                mMaterialDialog.dismiss();
                            }
                        })
                .setCanceledOnTouchOutside(false)
                .show();

    }

    /**
     * 注销
     *
     */
    private void loginoff() {
        HttpManager.getInstance().loginOff(InfoDao.getInstance().getConnectionId(),
                new loginOffResponseHandler());
    }

    class loginOffResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {
                //发送tcp断开
                Message msg = Message.obtain(null, M7Constant.HANDLER_LOGINOFF);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                clearData();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else{
                Toast.makeText(getActivity(), "网络不稳定，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearData() {
        //注销就清空原来保存的数据
        mEditor.putBoolean(M7Constant.SP_LOGIN_SUCCEED ,false);
        mEditor.commit();
        getActivity().getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);
        MessageDao.getInstance().deleteAllMsgs();
        NewMessageDao.getInstance().deleteAllMsgs();
        UserDao.getInstance().deleteUser();
        UserRoleDao.getInstance().deleteUserRole();
        MobileApplication.cacheUtil.clear();
    }

    class GetUserInfoResponseHandler implements ResponseListener{
        @Override
        public void onFailed() {

        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {
                user = HttpParser.getUserInfo(responseString);
                // 用户信息存入数据库
                UserDao.getInstance().deleteUser();
                UserRoleDao.getInstance().deleteUserRole();
                UserDao.getInstance().insertUser(user);
                List<String> userRoles = user.role;
                for (String role : userRoles) {
                    UserRole ur = new UserRole();
                    ur.role = role;
                    ur.user = user;
                    UserRoleDao.getInstance().insertUserRole(ur);
                }
                user_detail_tv_name.setText(user.displayName);
                user_detail_tv_num.setText(user.exten);
                user_detail_tv_email.setText(user.email);
                user_detail_tv_phone.setText(user.mobile);
                if(user.im_icon != null && !"".equals(user.im_icon)) {
                    GlideUtils.displayNet(contact_detail_image, user.im_icon+M7Constant.QINIU_IMG_ICON);
                }else {
                    GlideUtils.displayNative(contact_detail_image, R.drawable.img_default_head);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(conn);
    }


    private void initKeFu() {
        loadingDialog.show(getFragmentManager(), "kf");
        if (MobileApplication.isKFSDK) {
            getPeers();
        } else {
            startKFService();
        }
    }

    private void getPeers() {
        loadingDialog.dismiss();
        IMChatManager.getInstance().getPeers(new GetPeersListener() {
            @Override
            public void onSuccess(List<Peer> peers) {
                if (peers.size() > 1) {
                    PeerDialog dialog = new PeerDialog();
                    Bundle b = new Bundle();
                    b.putSerializable("Peers", (Serializable) peers);
                    b.putString("type", "init");
                    dialog.setArguments(b);
                    dialog.show(getFragmentManager(), "");

                } else if (peers.size() == 1) {
                    startChatActivity(peers.get(0).getId());
                } else {
                    startChatActivity("");
                }
            }

            @Override
            public void onFailed() {
                Toast.makeText(getActivity(), "获取技能组失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startKFService() {

        new Thread() {
            @Override
            public void run() {
                IMChatManager.getInstance().setOnInitListener(new InitListener() {
                    @Override
                    public void oninitSuccess() {
                        MobileApplication.isKFSDK = true;
                        getPeers();
                        LogUtil.d("sdk初始化成功");
                    }

                    @Override
                    public void onInitFailed() {
                        MobileApplication.isKFSDK = false;
                        Toast.makeText(getActivity(), "客服初始化失败", Toast.LENGTH_SHORT).show();
                        LogUtil.d("sdk初始化失败");
                    }
                });

                //初始化IMSdk,填入相关参数
//                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.moor.kefu.NEW_MSG", "1a407410-9ee1-11e5-a8e6-17b9721f92b3", user.displayName, user.exten);
                IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.moor.kefu.NEW_MSG", "1cf5bdb0-c66d-11e5-9875-63635d52845f", user.displayName, user.exten);
            }
        }.start();

    }

    /**
     * 启动客服聊天界面
     * @param peerId
     */
    private void startChatActivity(String peerId) {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra("PeerId", peerId);
        startActivity(chatIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0x1111:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initKeFu();
                }
                break;
        }
    }
}
