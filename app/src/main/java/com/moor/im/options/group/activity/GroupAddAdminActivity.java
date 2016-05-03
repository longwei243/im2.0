package com.moor.im.options.group.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.event.GroupUpdateSuccess;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Group;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.group.adapter.AllUserAdapter;
import com.moor.im.options.group.adapter.SelectedUserAdapter;
import com.moor.im.options.group.parser.GroupParser;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/4/27.
 */
public class GroupAddAdminActivity extends BaseActivity{

    private String sessionId;
    private ArrayList<Contacts> adminContacts = new ArrayList<Contacts>();
    private RecyclerView group_add_admin_rv_admin, group_add_admin_rv_members;
    private TextView group_add_admin_tv_count;

    private AllUserAdapter memberAdapter;
    private SelectedUserAdapter adminAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_admin);
        sessionId = getIntent().getStringExtra(M7Constant.GROUP_SESSION_ID);
        setTitleBar();
        Group group  = GroupParser.getInstance().getGroupById(sessionId);
        List<String> adminId = group.admin;
        if(adminId.size() != 0) {
            for (int i=0; i<adminId.size(); i++) {
                Contacts contact = ContactsDao.getInstance().getContactById(adminId.get(i));
                adminContacts.add(contact);
            }
        }

        group_add_admin_rv_admin = (RecyclerView) findViewById(R.id.group_add_admin_rv_admin);
        group_add_admin_rv_members = (RecyclerView) findViewById(R.id.group_add_admin_rv_members);
        group_add_admin_tv_count = (TextView) findViewById(R.id.group_add_admin_tv_count);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        group_add_admin_rv_members.setLayoutManager(linearLayoutManager);
        memberAdapter = new AllUserAdapter();
        group_add_admin_rv_members.setAdapter(memberAdapter);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        group_add_admin_rv_admin.setLayoutManager(linearLayoutManager1);
        adminAdapter = new SelectedUserAdapter();
        group_add_admin_rv_admin.setAdapter(adminAdapter);
        adminAdapter.setList(adminContacts);

        initMemberData(group);
        addRxBuxListener();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("添加管理员");
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
                addGroupAdmin(sessionId);
            }
        });
    }


    private void initMemberData(final Group group) {

        Observable<List<Contacts>> observable_db = Observable.create(new Observable.OnSubscribe<List<Contacts>>() {
            @Override
            public void call(Subscriber<? super List<Contacts>> subscriber) {
                List<String> memberIdList = group.member;
                List<String> adminIdList = group.admin;
                List<Contacts> members = new ArrayList<>();
                for(int j=0; j<adminIdList.size(); j++) {
                    for(int i=memberIdList.size()-1; i>=0; i--) {
                        if(adminIdList.get(j).equals(memberIdList.get(i))) {
                            memberIdList.remove(i);
                        }
                    }
                }
                for (int i=0; i<memberIdList.size(); i++) {
                    Contacts contacts = ContactsDao.getInstance().getContactById(memberIdList.get(i));
                    members.add(contacts);
                }
                subscriber.onNext(members);
            }
        });
        observable_db
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Contacts>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Contacts> contactsList) {
                        memberAdapter.setList(contactsList);
                        memberAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void addRxBuxListener() {
        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof UserChecked) {
                            //更新列表数据
                            Contacts contacts = ((UserChecked) event).contacts;
                            adminContacts.add(contacts);
                            adminAdapter.setList(adminContacts);
                            group_add_admin_rv_admin.scrollToPosition(adminContacts.size() - 1);
                            adminAdapter.notifyDataSetChanged();
                            LogUtil.d("接收到选中用户的通知"+contacts.displayName);
                        }else if (event instanceof UserUnCheck) {

                            Contacts contacts = ((UserUnCheck) event).contacts;
                            adminContacts.remove(contacts);
                            adminAdapter.setList(adminContacts);
                            group_add_admin_rv_admin.scrollToPosition(adminContacts.size() - 1);
                            adminAdapter.notifyDataSetChanged();
                            LogUtil.d("接收到取消选中用户的通知"+contacts.displayName);
                        }
                        group_add_admin_tv_count.setText(adminContacts.size()+"人");

                    }
                }));
    }

    private void addGroupAdmin(String groupId) {
        showLoadingDialog();
        ArrayList<String> adminIds = new ArrayList<>();
        for(int i=0; i<adminContacts.size(); i++) {
            adminIds.add(adminContacts.get(i)._id);
        }
        Observable createGroupObservable = HttpManager.getInstance().addGroupAdmin(InfoDao.getInstance().getConnectionId(), groupId, adminIds);
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
                        Toast.makeText(GroupAddAdminActivity.this, "群组管理员添加成功", Toast.LENGTH_SHORT).show();
                        RxBus.getInstance().send(new GroupUpdateSuccess());
                        finish();
                    }
                });

    }
}
