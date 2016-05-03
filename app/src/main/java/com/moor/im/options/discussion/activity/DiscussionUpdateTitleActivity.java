package com.moor.im.options.discussion.activity;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.event.DiscussionCreateSuccess;
import com.moor.im.common.event.DiscussionUpdateSuccess;
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
public class DiscussionUpdateTitleActivity extends BaseActivity{

    private TextInputLayout group_update_title_til_name;
    String sessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_update_title);
        sessionId = getIntent().getStringExtra(M7Constant.DISCUSSION_SESSION_ID);
        setTitleBar();
        initViews();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("修改讨论组名称");
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
                    Toast.makeText(DiscussionUpdateTitleActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                }else {
                    updateDiscussionTitle(sessionId, groupName);
                }
            }
        });
    }

    private void updateDiscussionTitle(String groupId, String groupName) {
        showLoadingDialog();
        Observable updateDiscussionTitleObservable = HttpManager.getInstance().updateDiscussionTitle(InfoDao.getInstance().getConnectionId(), groupId, groupName);
        updateDiscussionTitleObservable
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
                        Toast.makeText(DiscussionUpdateTitleActivity.this, "讨论组名称修改成功", Toast.LENGTH_SHORT).show();
                        RxBus.getInstance().send(new DiscussionUpdateSuccess());
                        finish();
                    }
                });

    }

    private void initViews() {
        group_update_title_til_name = (TextInputLayout) findViewById(R.id.group_update_title_til_name);
    }

}
