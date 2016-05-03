package com.moor.im.options.setup;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.db.dao.UserRoleDao;
import com.moor.im.common.event.MsgRead;
import com.moor.im.common.event.NewMsgReceived;
import com.moor.im.common.event.SendMsg;
import com.moor.im.common.event.UserInfoUpdate;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.model.UserRole;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.setup.activity.ClipImageViewActivity;
import com.moor.im.options.setup.activity.EditActivity;

import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by longwei on 2016/3/16.
 */
public class SetupFragment extends BaseLazyFragment{
    RelativeLayout setup_ll_loginoff, setup_ll_update,
            setup_ll_aboutme, setup_ll_icon,
            setup_ll_edit_name, setup_ll_edit_email,
            setup_ll_edit_phone, setup_ll_kefu, setup_ll_mobile, setup_ll_mobile_erp;

    TextView user_detail_tv_name, user_detail_tv_num, user_detail_tv_email, user_detail_tv_phone;

    ImageView contact_detail_image;
    private User user = UserDao.getInstance().getUser();
    private CompositeSubscription _subscriptions;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, null);
        setup_ll_loginoff = (RelativeLayout) view.findViewById(R.id.setup_ll_loginoff);
        setup_ll_loginoff.setOnClickListener(clickListener);

        setup_ll_update = (RelativeLayout) view.findViewById(R.id.setup_ll_update);
        setup_ll_update.setOnClickListener(clickListener);

        setup_ll_aboutme = (RelativeLayout) view.findViewById(R.id.setup_ll_aboutme);
        setup_ll_aboutme.setOnClickListener(clickListener);

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


        contact_detail_image = (ImageView) view.findViewById(R.id.user_icon);

        if(user.im_icon != null && !"".equals(user.im_icon)) {
            GlideUtils.displayNet(contact_detail_image, user.im_icon+M7Constant.QINIU_IMG_ICON);
        }else {
            GlideUtils.displayNative(contact_detail_image, R.drawable.img_default_head);
        }

        setup_ll_kefu = (RelativeLayout) view.findViewById(R.id.setup_ll_kefu);
        setup_ll_kefu.setOnClickListener(clickListener);


        setup_ll_mobile = (RelativeLayout) view.findViewById(R.id.setup_ll_mobile);
        setup_ll_mobile.setOnClickListener(clickListener);

        setup_ll_mobile_erp = (RelativeLayout) view.findViewById(R.id.setup_ll_mobile_erp);
        setup_ll_mobile_erp.setOnClickListener(clickListener);

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

                    break;
                case R.id.setup_ll_update:
//                    Intent updateIntent = new Intent(getActivity(), UpdateActivity.class);
//                    startActivity(updateIntent);
                    break;
                case R.id.setup_ll_aboutme:
//                    Intent aboutIntent = new Intent(getActivity(), AboutMeActivity.class);
//                    startActivity(aboutIntent);
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

                    break;
                case R.id.setup_ll_mobile:
//                    Intent mobileIntent = new Intent(SetupFragment.this.getActivity(), MACdrActivity.class);
//                    startActivity(mobileIntent);
                    break;
                case R.id.setup_ll_mobile_erp:
//                    Intent erpIntent = new Intent(SetupFragment.this.getActivity(), MAErpActivity.class);
//                    startActivity(erpIntent);
                    break;
            }
        }
    };


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
}
