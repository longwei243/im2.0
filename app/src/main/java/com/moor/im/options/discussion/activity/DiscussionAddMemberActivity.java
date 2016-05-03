package com.moor.im.options.discussion.activity;

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
import com.moor.im.common.event.DiscussionUpdateSuccess;
import com.moor.im.common.event.GroupUpdateSuccess;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.Group;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.discussion.parser.DiscussionParser;
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
public class DiscussionAddMemberActivity extends BaseActivity{

    private String sessionId;
    private ArrayList<Contacts> selectedContacts = new ArrayList<Contacts>();
    private RecyclerView group_add_member_rv_selected, group_add_member_rv_users;
    private TextView group_add_member_tv_count;

    private AllUserAdapter userAdapter;
    private SelectedUserAdapter selectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_member);
        sessionId = getIntent().getStringExtra(M7Constant.DISCUSSION_SESSION_ID);
        setTitleBar();
        Discussion discussion  = DiscussionParser.getInstance().getDiscussionById(sessionId);
        group_add_member_rv_selected = (RecyclerView) findViewById(R.id.group_add_admin_rv_admin);
        group_add_member_rv_users = (RecyclerView) findViewById(R.id.group_add_admin_rv_members);
        group_add_member_tv_count = (TextView) findViewById(R.id.group_add_admin_tv_count);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        group_add_member_rv_users.setLayoutManager(linearLayoutManager);
        userAdapter = new AllUserAdapter();
        group_add_member_rv_users.setAdapter(userAdapter);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        group_add_member_rv_selected.setLayoutManager(linearLayoutManager1);
        selectAdapter = new SelectedUserAdapter();
        group_add_member_rv_selected.setAdapter(selectAdapter);
        selectAdapter.setList(selectedContacts);

        initMemberData(discussion);
        addRxBuxListener();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("添加新成员");
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
                addMemberAdmin(sessionId);
            }
        });
    }


    private void initMemberData(final Discussion discussion) {

        Observable<List<Contacts>> observable_db = Observable.create(new Observable.OnSubscribe<List<Contacts>>() {
            @Override
            public void call(Subscriber<? super List<Contacts>> subscriber) {
                List<String> memberIdList = discussion.member;
                List<Contacts> alluser = ContactsDao.getInstance().getContacts();
                for(int j=0; j<memberIdList.size(); j++) {
                    for(int i=alluser.size()-1; i>=0; i--) {
                        if(memberIdList.get(j).equals(alluser.get(i)._id)) {
                            alluser.remove(i);
                        }
                    }
                }
                subscriber.onNext(alluser);
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
                        userAdapter.setList(contactsList);
                        userAdapter.notifyDataSetChanged();
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
                            selectedContacts.add(contacts);
                            selectAdapter.setList(selectedContacts);
                            group_add_member_rv_selected.scrollToPosition(selectedContacts.size() - 1);
                            selectAdapter.notifyDataSetChanged();
                            LogUtil.d("接收到选中用户的通知"+contacts.displayName);
                        }else if (event instanceof UserUnCheck) {

                            Contacts contacts = ((UserUnCheck) event).contacts;
                            selectedContacts.remove(contacts);
                            selectAdapter.setList(selectedContacts);
                            group_add_member_rv_selected.scrollToPosition(selectedContacts.size() - 1);
                            selectAdapter.notifyDataSetChanged();
                            LogUtil.d("接收到取消选中用户的通知"+contacts.displayName);
                        }
                        group_add_member_tv_count.setText(selectedContacts.size()+"人");

                    }
                }));
    }

    private void addMemberAdmin(String groupId) {
        showLoadingDialog();
        ArrayList<String> memberIds = new ArrayList<>();
        for(int i = 0; i< selectedContacts.size(); i++) {
            memberIds.add(selectedContacts.get(i)._id);
        }
        Observable createGroupObservable = HttpManager.getInstance().addDiscussionMember(InfoDao.getInstance().getConnectionId(), groupId, memberIds);
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
                        Toast.makeText(DiscussionAddMemberActivity.this, "讨论组成员添加成功", Toast.LENGTH_SHORT).show();
                        RxBus.getInstance().send(new DiscussionUpdateSuccess());
                        finish();
                    }
                });

    }
}
