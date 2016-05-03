package com.moor.im.options.group.activity;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.event.GroupUpdateSuccess;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.base.BaseActivity;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/4/27.
 */
public class GroupUpdateTitleActivity extends BaseActivity{

    private TextInputLayout group_update_title_til_name;
    String sessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_update_title);
        sessionId = getIntent().getStringExtra(M7Constant.GROUP_SESSION_ID);
        setTitleBar();
        initViews();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("修改群名称");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton titlebar_done = (ImageButton) findViewById(R.id.titlebar_done);
        titlebar_done.setVisibility(View.VISIBLE);
        titlebar_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = group_update_title_til_name.getEditText().getText().toString().trim();
                if("".equals(groupName)) {
                    Toast.makeText(GroupUpdateTitleActivity.this, "请输入群名称", Toast.LENGTH_SHORT).show();
                }else {
                    updateGroupTitle(sessionId, groupName);
                }
            }
        });
    }

    private void updateGroupTitle(String groupId, String groupName) {
        showLoadingDialog();
        Observable createGroupObservable = HttpManager.getInstance().updateGroupTitle(InfoDao.getInstance().getConnectionId(), groupId, groupName);
        createGroupObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onNext(Object o) {
                        //成功了
                        dismissLoadingDialog();
                        Toast.makeText(GroupUpdateTitleActivity.this, "群组名称修改成功", Toast.LENGTH_SHORT).show();
                        RxBus.getInstance().send(new GroupUpdateSuccess());
                        finish();
                    }
                });

    }

    private void initViews() {
        group_update_title_til_name = (TextInputLayout) findViewById(R.id.group_update_title_til_name);
    }

}
