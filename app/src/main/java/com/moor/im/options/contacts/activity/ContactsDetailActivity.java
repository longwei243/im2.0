package com.moor.im.options.contacts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.User;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.imageviewlook.ImageViewLookActivity;

/**
 * Created by longwei on 2016/4/19.
 */
public class ContactsDetailActivity extends BaseActivity implements View.OnClickListener{

    private String _id, otherName;
    private Button mSendMessage, mCallPhone;
    private TextView contact_detail_tv_name,
            contact_detail_tv_num,
            contact_detail_tv_phone,
            contact_detail_tv_email,
            contact_detail_tv_product;

    private ImageView contact_detail_image;


    User user = UserDao.getInstance().getUser();

    private Contacts contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        setTitleBar();
        Intent intent = getIntent();
        contact = (Contacts) intent.getSerializableExtra("contact");
        _id = contact._id;
        otherName = contact.displayName;
        init();
        registerListener();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("联系人详情");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // 初始化方法
    public void init() {
        mSendMessage = (Button) this.findViewById(R.id.send_message);
        mCallPhone = (Button) this.findViewById(R.id.call_phone);
        contact_detail_tv_name = (TextView) findViewById(R.id.contact_detail_tv_name);
        contact_detail_tv_num = (TextView) findViewById(R.id.contact_detail_tv_num);
        contact_detail_tv_phone = (TextView) findViewById(R.id.contact_detail_tv_phone);
        contact_detail_tv_email = (TextView) findViewById(R.id.contact_detail_tv_email);
        contact_detail_tv_product = (TextView) findViewById(R.id.contact_detail_tv_product);
        if("".equals(contact.mobile)) {
            mCallPhone.setVisibility(View.GONE);
            contact_detail_tv_phone.setText("未绑定");
        }else{
            contact_detail_tv_phone.setText(contact.mobile);
        }
        if("".equals(contact.email)) {
            contact_detail_tv_email.setText("未绑定");
        }else{
            contact_detail_tv_email.setText(contact.email);
        }
        contact_detail_tv_name.setText(contact.displayName);
        contact_detail_tv_num.setText(contact.exten);
        if("zj".equals(contact.product)) {
            contact_detail_tv_product.setText("企业总机");
        }else if("cc".equals(contact.product)){
            contact_detail_tv_product.setText("联络中心");
        }

        contact_detail_image = (ImageView) findViewById(R.id.contact_detail_image);
        final String im_icon = contact.im_icon;
        if(im_icon != null && !"".equals(im_icon)) {
            Glide.with(this).load(im_icon+ M7Constant.QINIU_IMG_ICON).asBitmap().placeholder(R.drawable.img_default_head).into(contact_detail_image);
        }else {
            Glide.with(this).load(R.drawable.img_default_head).asBitmap().into(contact_detail_image);
        }
        contact_detail_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(im_icon != null && !"".equals(im_icon)) {
                    Intent imageLookIntent = new Intent(ContactsDetailActivity.this, ImageViewLookActivity.class);
                    imageLookIntent.putExtra(M7Constant.IMG_PATH, im_icon);
                    startActivity(imageLookIntent);
                }
            }
        });


    }

    // 注册监听方法
    public void registerListener() {
        mSendMessage.setOnClickListener(this);
        mCallPhone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_message:

                Intent chat = new Intent(ContactsDetailActivity.this,
                        ChatActivity.class);
                chat.putExtra("otherName", otherName);
                chat.putExtra("type", "User");
                chat.putExtra("_id", _id);

                startActivity(chat);
                break;
        }
    }
}
