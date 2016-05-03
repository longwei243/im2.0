package com.moor.im.options.group.activity;

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
import com.moor.im.common.event.GroupUpdateSuccess;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Group;
import com.moor.im.common.model.GroupAdminAndMembers;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.base.BaseActivity;
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
public class GroupSettingActivity extends BaseActivity{

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
        sessionId = getIntent().getStringExtra(M7Constant.GROUP_SESSION_ID);
        group_setting_list = (ListView) findViewById(R.id.group_setting_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Group group = GroupParser.getInstance().getGroupById(sessionId);
        if(group == null) {
            return;
        }

        List<String> admins = group.admin;
        for (int i=0; i<admins.size(); i++) {
            if(user._id.equals(admins.get(i))) {
                toolbar.inflateMenu(R.menu.group_setting);
                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_change_group_name:
                                Intent intent = new Intent(GroupSettingActivity.this, GroupUpdateTitleActivity.class);
                                intent.putExtra(M7Constant.GROUP_SESSION_ID, sessionId);
                                startActivity(intent);                                break;
                            case R.id.action_add_group_admin:
                                Intent intent1 = new Intent(GroupSettingActivity.this, GroupAddAdminActivity.class);
                                intent1.putExtra(M7Constant.GROUP_SESSION_ID, sessionId);
                                startActivity(intent1);
                                break;
                            case R.id.action_add_group_member:
                                Intent intent2 = new Intent(GroupSettingActivity.this, GroupAddMemberActivity.class);
                                intent2.putExtra(M7Constant.GROUP_SESSION_ID, sessionId);
                                startActivity(intent2);
                                break;
                            case R.id.action_delete_group:
                                    showDeleteDialog();
                                break;
                        }
                        return false;
                    }
                });
            }
        }

        for (int i=0; i<admins.size(); i++) {
            GroupAdminAndMembers gaam = new GroupAdminAndMembers();
            gaam.set_id(admins.get(i));
            String name = ContactsDao.getInstance().getContactsName(admins.get(i));
            String icicon = ContactsDao.getInstance().getContactsIcon(admins.get(i));
            if(name != null && !"".equals(name)) {
                gaam.setName(name);
                gaam.setType("Admin");
                gaam.setImicon(icicon);
                adminAndMemberses.add(gaam);
            }

        }

        List<String> members = group.member;
        List<String> tempMembers = members;
        for (int i=0; i<admins.size(); i++) {
            for (int j=tempMembers.size()-1; j>=0; j--) {
                if(admins.get(i).equals(tempMembers.get(j))) {
                    tempMembers.remove(j);
                }
            }
        }
        for (int i=0; i<tempMembers.size(); i++) {
            GroupAdminAndMembers gaam = new GroupAdminAndMembers();
            gaam.set_id(tempMembers.get(i));
            String name = ContactsDao.getInstance().getContactsName(tempMembers.get(i));
            String icicon = ContactsDao.getInstance().getContactsIcon(tempMembers.get(i));
            if(name != null && !"".equals(name)) {
                gaam.setName(name);
                gaam.setType("Member");
                gaam.setImicon(icicon);
                adminAndMemberses.add(gaam);
            }

        }

        GroupAdminAndMemberAdapter adapter = new GroupAdminAndMemberAdapter(GroupSettingActivity.this, adminAndMemberses);
        group_setting_list.setAdapter(adapter);
        addRxBuxListener();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("群组信息");
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
                        if (event instanceof GroupUpdateSuccess) {
                            finish();
                        }
                    }
                }));
    }

    private void showDeleteDialog() {
        mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle("温馨提示")
                .setMessage("确认删除该群组吗?")
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        deleteGroup(sessionId);
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


    private void deleteGroup(String groupId) {
        Observable deleteGroupObservable = HttpManager.getInstance().deleteGroup(InfoDao.getInstance().getConnectionId(), groupId);
        deleteGroupObservable
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
                        Toast.makeText(GroupSettingActivity.this, "群组删除成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }
}
