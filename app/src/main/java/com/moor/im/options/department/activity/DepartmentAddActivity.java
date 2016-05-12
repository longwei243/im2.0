package com.moor.im.options.department.activity;

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
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.event.DiscussionCreateSuccess;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Info;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.utils.ObservableUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.department.model.Department;
import com.moor.im.options.department.parser.DepartmentParser;
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
 * Created by longwei on 2016/5/12.
 */
public class DepartmentAddActivity extends BaseActivity{

    private TextInputLayout group_create_til_name, group_create_til_desc;
    private RecyclerView group_create_rv_selected_user, group_create_rv_all_user;
    private TextView group_create_tv_usercount;

    private AllUserAdapter allUesrAdapter;
    private SelectedUserAdapter selectedUserAdapter;

    private List<Contacts> selectedUserList = new ArrayList<>();

    boolean isRoot;
    private String rootId;
    TextView titlebar_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_department);
        setTitleBar();
        initViews();

        Intent intent = getIntent();
        if(intent.getStringExtra("departmentId") == null) {
//			System.out.println("添加根部门");
            isRoot = true;
            titlebar_name.setText("添加根部门");
        }else {
//			System.out.println("添加子部门");
            titlebar_name.setText("添加子部门");
            isRoot = false;
            rootId = intent.getStringExtra("departmentId");
//			System.out.println("传过来的部门id是："+rootId);
        }
    }

    private void setTitleBar() {
        titlebar_name = (TextView) findViewById(R.id.titlebar_name);

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
                String name = group_create_til_name.getEditText().getText().toString().trim();
                String desc = group_create_til_desc.getEditText().getText().toString().trim();
                if("".equals(name)) {
                    Toast.makeText(DepartmentAddActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                }else if(selectedUserList.size() == 0) {
                    Toast.makeText(DepartmentAddActivity.this, "请选择成员", Toast.LENGTH_SHORT).show();
                }else {
                    createDepartment(name, desc);
                }
            }
        });
    }

    private void initViews() {
        group_create_til_name = (TextInputLayout) findViewById(R.id.group_create_til_name);
        group_create_til_desc = (TextInputLayout) findViewById(R.id.group_create_til_desc);
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

    private void createDepartment(String name, String desc) {
        showLoadingDialog();
        ArrayList<String> memberList = new ArrayList<>();
        for (int i = 0; i < selectedUserList.size(); i++) {
            memberList.add(selectedUserList.get(i)._id);
        }

        HttpManager.getInstance().addDepartment(InfoDao.getInstance().getConnectionId(), memberList, new ArrayList(), name, desc, isRoot, new AddDepartmentResponseHandler());


    }

    class AddDepartmentResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            Toast.makeText(DepartmentAddActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {
                if(isRoot){
                    //若添加根部门成功
                    //通知获取最新部门信息数据，刷新界面
                    finish();
                    Intent intent = new Intent("department_update");
                    sendBroadcast(intent);
                }else {
                    //添加子部门成功，先将该部门信息获得
                    Department dept = HttpParser.getDepartmentInfo(responseString);
                    //更新它的父部门信息
                    List<Department> departments = HttpParser.getDepartments((MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT)));
                    DepartmentParser dp = new DepartmentParser(departments);
                    Department rootDept = dp.getDepartmentById(rootId);
                    String id = NullUtil.checkNull(rootDept._id);
                    String name = NullUtil.checkNull(rootDept.Name);
                    String desc = NullUtil.checkNull(rootDept.Description);
                    boolean isRoot = rootDept.Root;
                    ArrayList members = (ArrayList) rootDept.Members;
                    ArrayList subDept = (ArrayList) rootDept.Subdepartments;
                    subDept.add(dept._id);
                    //发送网络请求，更新根部门信息
                    HttpManager.getInstance().updateDepartment(InfoDao.getInstance().getConnectionId(), id, members, subDept, name, desc, isRoot, new UpdateDepartmentResponseHandler());
                }

//				System.out.println("添加部门返回的数据是："+responseString);

            } else {
                Toast.makeText(DepartmentAddActivity.this, "失败", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    class UpdateDepartmentResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            Toast.makeText(DepartmentAddActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onSuccess(String responseString) {

            if (HttpParser.getSucceed(responseString)) {

//				System.out.println("更新部门信息返回结果："+responseString);
                finish();
                //更新成功了，通知获取最新部门信息数据，刷新界面
                Intent intent = new Intent("department_update");
                sendBroadcast(intent);
            } else {
                Toast.makeText(DepartmentAddActivity.this, "失败", Toast.LENGTH_SHORT)
                        .show();

            }
        }
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
