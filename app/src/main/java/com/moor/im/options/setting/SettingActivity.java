package com.moor.im.options.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.event.MsgRead;
import com.moor.im.common.model.NewMessage;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.aboutme.AboutMeActivity;
import com.moor.im.options.base.BaseActivity;

import java.util.UUID;

/**
 * Created by longwei on 2016/5/19.
 */
public class SettingActivity extends BaseActivity{

    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;
    private CheckBox setting_cb_ma;
    private LinearLayout setting_ll_aboutme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("设置");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mSp = getSharedPreferences(M7Constant.MAIN_SP, 0);
        mEditor = mSp.edit();

        setting_ll_aboutme = (LinearLayout) findViewById(R.id.setting_ll_aboutme);
        setting_ll_aboutme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutIntent = new Intent(SettingActivity.this, AboutMeActivity.class);
                startActivity(aboutIntent);
            }
        });

        setting_cb_ma = (CheckBox) findViewById(R.id.setting_cb_ma);
        boolean isMa = mSp.getBoolean("ma", true);
        if(isMa) {
            setting_cb_ma.setChecked(true);
        }else {
            setting_cb_ma.setChecked(false);
        }

        setting_cb_ma.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    mEditor.putBoolean("ma", true);
                    mEditor.commit();
                    buildMAMsg();
                    RxBus.getInstance().send(new MsgRead());
                }else {
                    mEditor.putBoolean("ma", false);
                    mEditor.commit();
                    NewMessageDao.getInstance().deleteMsgById("MA");
                    RxBus.getInstance().send(new MsgRead());
                }
            }
        });

    }

    private void buildMAMsg() {
        NewMessage maMsg = new NewMessage();
        maMsg._id = UUID.randomUUID().toString();
        maMsg.isTop = 0;
        maMsg.fromName = "客服助手";
        maMsg.message = "查询通话记录，处理工单";
        maMsg.msgType = "";
        maMsg.sessionId = "MA";
        maMsg.time = System.currentTimeMillis();
        maMsg.img = "";
        maMsg.unReadCount = 0;
        maMsg.type = "MA";
        maMsg.from = "";
        NewMessageDao.getInstance().insertMAMsg(maMsg);
    }
}