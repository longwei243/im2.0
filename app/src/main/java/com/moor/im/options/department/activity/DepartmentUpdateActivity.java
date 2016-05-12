package com.moor.im.options.department.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.ObservableUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.GridViewInScrollView;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.department.DepartmentActivityUtil;
import com.moor.im.options.department.model.Department;
import com.moor.im.options.department.parser.DepartmentParser;
import com.moor.im.options.group.adapter.AllUserAdapter;
import com.moor.im.options.group.adapter.SelectedUserAdapter;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DepartmentUpdateActivity extends BaseActivity{
	private TextInputLayout group_create_til_name, group_create_til_desc;
	private RecyclerView group_create_rv_selected_user, group_create_rv_all_user;
	private TextView group_create_tv_usercount;

	private AllUserAdapter allUesrAdapter;
	private SelectedUserAdapter selectedUserAdapter;

	private List<Contacts> selectedUserList = new ArrayList<>();
		
	private String departmentId;
	private Department department;
	TextView titlebar_name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DepartmentActivityUtil.getInstance().add(this);
		setContentView(R.layout.activity_create_department);
		setTitleBar();
		Intent intent = getIntent();
		departmentId = intent.getStringExtra("departmentId");
		DepartmentParser dp = new DepartmentParser(HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT)));
		department = dp.getDepartmentById(departmentId);
		initViews();

	}

	private void setTitleBar() {
		titlebar_name = (TextView) findViewById(R.id.titlebar_name);
		titlebar_name.setText("修改部门");
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
					Toast.makeText(DepartmentUpdateActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
				}else if(selectedUserList.size() == 0) {
					Toast.makeText(DepartmentUpdateActivity.this, "请选择成员", Toast.LENGTH_SHORT).show();
				}else {
					updateDepartment(name, desc);
				}
			}
		});
	}

	private void updateDepartment(String name, String desc) {
		showLoadingDialog();
		ArrayList<String> memberList = new ArrayList<>();
		for (int i = 0; i < selectedUserList.size(); i++) {
			memberList.add(selectedUserList.get(i)._id);
		}

		HttpManager.getInstance().updateDepartment(InfoDao.getInstance().getConnectionId(), departmentId, memberList, (ArrayList)department.Subdepartments, name, desc, department.Root, new UpdateDepartmentResponseHandler());

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

	
	class UpdateDepartmentResponseHandler implements ResponseListener {
		@Override
		public void onFailed() {
			Toast.makeText(DepartmentUpdateActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onSuccess(String responseString) {

			if (HttpParser.getSucceed(responseString)) {

				System.out.println("更新部门信息返回结果："+responseString);
				Toast.makeText(DepartmentUpdateActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
				//更新成功了，通知获取最新部门信息数据，刷新界面
				Intent intent = new Intent("department_update");
				sendBroadcast(intent);
				
				DepartmentActivityUtil.getInstance().exit();
				
				Intent it = new Intent(DepartmentUpdateActivity.this, DepartmentActivity.class);
				startActivity(it);
			} else {
//				Toast.makeText(DepartmentUpdateActivity.this, message, Toast.LENGTH_SHORT)
//						.show();

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
