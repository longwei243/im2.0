package com.moor.im.options.discussion.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.MaterialDialog;
import com.moor.im.common.event.DiscussionUpdateSuccess;
import com.moor.im.common.event.GroupUpdateSuccess;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.Group;
import com.moor.im.common.model.GroupAdminAndMembers;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.discussion.parser.DiscussionParser;
import com.moor.im.options.group.activity.GroupAddAdminActivity;
import com.moor.im.options.group.activity.GroupAddMemberActivity;
import com.moor.im.options.group.activity.GroupUpdateTitleActivity;
import com.moor.im.options.group.adapter.GroupAdminAndMemberAdapter;
import com.moor.im.options.group.parser.GroupParser;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/4/27.
 */
public class DiscussionSettingActivity extends BaseActivity{

    private String sessionId;
    private ListView group_setting_list;
    User user = UserDao.getInstance().getUser();

    List<GroupAdminAndMembers> adminAndMemberses = new ArrayList<GroupAdminAndMembers>();
    MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);
        setTitleBar();
        sessionId = getIntent().getStringExtra(M7Constant.DISCUSSION_SESSION_ID);
        group_setting_list = (ListView) findViewById(R.id.group_setting_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Discussion discussion = DiscussionParser.getInstance().getDiscussionById(sessionId);

        toolbar.inflateMenu(R.menu.discussion_setting);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_change_discussion_name:
                        Intent intent = new Intent(DiscussionSettingActivity.this, DiscussionUpdateTitleActivity.class);
                        intent.putExtra(M7Constant.DISCUSSION_SESSION_ID, sessionId);
                        startActivity(intent);                                break;
                    case R.id.action_add_discussion_member:
                        Intent intent2 = new Intent(DiscussionSettingActivity.this, DiscussionAddMemberActivity.class);
                        intent2.putExtra(M7Constant.DISCUSSION_SESSION_ID, sessionId);
                        startActivity(intent2);
                        break;
                    case R.id.action_delete_discussion:
                        showDeleteDialog();
                        break;
                }
                return false;
            }
        });

        List<String> members = discussion.member;

        for (int i=0; i<members.size(); i++) {
            GroupAdminAndMembers gaam = new GroupAdminAndMembers();
            gaam.set_id(members.get(i));
            String name = ContactsDao.getInstance().getContactsName(members.get(i));
            String icicon = ContactsDao.getInstance().getContactsIcon(members.get(i));
            if(name != null && !"".equals(name)) {
                gaam.setName(name);
                gaam.setType("Member");
                gaam.setImicon(icicon);
                adminAndMemberses.add(gaam);
            }
        }
        GroupAdminAndMemberAdapter adapter = new GroupAdminAndMemberAdapter(DiscussionSettingActivity.this, adminAndMemberses);
        group_setting_list.setAdapter(adapter);
        addRxBuxListener();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("讨论组信息");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addRxBuxListener() {
        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof DiscussionUpdateSuccess) {
                            finish();
                        }
                    }
                }));
    }

    private void showDeleteDialog() {
        mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle("温馨提示")
                .setMessage("确认删除该讨论组吗?")
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        deleteDiscussion(sessionId);
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


    private void deleteDiscussion(String groupId) {
        Observable deleteDiscussinObservable = HttpManager.getInstance().deleteDiscussion(InfoDao.getInstance().getConnectionId(), groupId);
        deleteDiscussinObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {
                        //成功了
                        Toast.makeText(DiscussionSettingActivity.this, "讨论组删除成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }
}
