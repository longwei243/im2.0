package com.moor.im.options.group.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.event.GroupCreateSuccess;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.ObservableUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.group.adapter.AllUserAdapter;
import com.moor.im.options.group.adapter.SelectedUserAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/4/26.
 */
public class CreateGroupActivity extends BaseActivity{

    private TextInputLayout group_create_til_name;
    private RecyclerView group_create_rv_selected_user, group_create_rv_all_user;
    private TextView group_create_tv_usercount;

    private AllUserAdapter allUesrAdapter;
    private SelectedUserAdapter selectedUserAdapter;

    private List<Contacts> selectedUserList = new ArrayList<>();

    private User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setTitleBar();
        initViews();
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("创建群组");
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
                String groupName = group_create_til_name.getEditText().getText().toString().trim();
                if("".equals(groupName)) {
                    Toast.makeText(CreateGroupActivity.this, "请输入群名称", Toast.LENGTH_SHORT).show();
                }else if(selectedUserList.size() == 0) {
                    Toast.makeText(CreateGroupActivity.this, "请选择群成员", Toast.LENGTH_SHORT).show();
                }else {
                    createGroup(groupName);
                }
            }
        });
    }

    private void initViews() {
        group_create_til_name = (TextInputLayout) findViewById(R.id.group_create_til_name);
        group_create_rv_selected_user = (RecyclerView) findViewById(R.id.group_create_rv_selected_user);
        group_create_rv_all_user = (RecyclerView) findViewById(R.id.group_create_rv_all_user);
        group_create_tv_usercount = (TextView) findViewById(R.id.group_create_tv_usercount);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        group_create_rv_all_user.setLayoutManager(linearLayoutManager);
        allUesrAdapter = new AllUserAdapter();
        group_create_rv_all_user.setAdapter(allUesrAdapter);

        initAllUserData();

        addRxBuxListener();

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        group_create_rv_selected_user.setLayoutManager(linearLayoutManager1);
        selectedUserAdapter = new SelectedUserAdapter();
        group_create_rv_selected_user.setAdapter(selectedUserAdapter);

    }

    private void initAllUserData() {
        Observable<List<Contacts>> observable_db = ObservableUtils.getContactsFormDB();
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
                        allUesrAdapter.setList(contactsList);
                        allUesrAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void createGroup(String groupName) {
        showLoadingDialog();
        ArrayList<String> adminList = new ArrayList<>();
        ArrayList<String> memberList = new ArrayList<>();

        adminList.add(user._id);
        memberList.add(user._id);
        for (int i = 0; i < selectedUserList.size(); i++) {
            memberList.add(selectedUserList.get(i)._id);
        }


        Observable createGroupObservable = HttpManager.getInstance().createGroup(InfoDao.getInstance().getConnectionId(), adminList, memberList, groupName);
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
                        //创建成功了
                        dismissLoadingDialog();
                        Toast.makeText(CreateGroupActivity.this, "群组创建成功", Toast.LENGTH_SHORT).show();
                        RxBus.getInstance().send(new GroupCreateSuccess());
                        finish();
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
                    selectedUserList.add(contacts);
                    selectedUserAdapter.setList(selectedUserList);
                    group_create_rv_selected_user.scrollToPosition(selectedUserList.size() - 1);
                    selectedUserAdapter.notifyDataSetChanged();
                    LogUtil.d("接收到选中用户的通知"+contacts.displayName);
                }else if (event instanceof UserUnCheck) {

                    Contacts contacts = ((UserUnCheck) event).contacts;
                    selectedUserList.remove(contacts);
                    selectedUserAdapter.setList(selectedUserList);
                    group_create_rv_selected_user.scrollToPosition(selectedUserList.size() - 1);
                    selectedUserAdapter.notifyDataSetChanged();
                    LogUtil.d("接收到取消选中用户的通知"+contacts.displayName);
                }
                group_create_tv_usercount.setText(selectedUserList.size()+"人");

            }
        }));
    }
}
