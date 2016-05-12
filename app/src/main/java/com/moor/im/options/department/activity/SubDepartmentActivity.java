package com.moor.im.options.department.activity;

import java.util.ArrayList;
import java.util.List;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.MaterialDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.contacts.activity.ContactsDetailActivity;
import com.moor.im.options.department.DepartmentActivityUtil;
import com.moor.im.options.department.adapter.SubDepartmentAdapter;
import com.moor.im.options.department.model.Department;
import com.moor.im.options.department.model.DeptAndMember;
import com.moor.im.options.department.parser.DepartmentParser;

/**
 * 子部门界面
 * 删除子部门有bug，父部门中没有将子部门信息删除
 * @author LongWei
 *
 */
public class SubDepartmentActivity extends BaseActivity{

	private ListView mListView;
	
	private String departmentId;
	
	private List<DeptAndMember> deptAndMembers = new ArrayList<DeptAndMember>();

	private SubDepartmentAdapter adapter;

	private ImageView title_btn_back;

	private User user = UserDao.getInstance().getUser();

	private AlertDialog alert;
	MaterialDialog mMaterialDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DepartmentActivityUtil.getInstance().add(this);
		setContentView(R.layout.activity_subdepartment);
		setupTitleBar();
		deptAndMembers.clear();
		
		//获取了上级部门的Id
		Intent intent = getIntent();
		departmentId = intent.getStringExtra("departmentId");
		if(departmentId == null) {
			departmentId = "";
		}
//		System.out.println("子部门界面接收到的id是："+departmentId);
		
		List<Department> departments = HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT));
		DepartmentParser dp = new DepartmentParser(departments);
		Department rootDepartment = dp.getDepartmentById(departmentId);
		if(rootDepartment == null) {
			return;
		}
		//取得子部门
		List<Department> subDept = dp.getSecondDepartments(rootDepartment);
//		System.out.println("有多少了子部门："+subDept.size());
		for (int i = 0; i < subDept.size(); i++) {
			
			String id = NullUtil.checkNull(subDept.get(i)._id);
			String name = NullUtil.checkNull(subDept.get(i).Name);
			if("".equals(name)) {
				name = "部门名称是空的";
			}
			String type = "dept";
//			System.out.println("部门id是："+id);
//			System.out.println("部门名称是："+name);
			DeptAndMember dam = new DeptAndMember();
			dam.setId(id);
			dam.setName(name);
			dam.setType(type);
			deptAndMembers.add(dam);
		}
		//取得成员
		List<Contacts> members = dp.getMembers(rootDepartment);
//		System.out.println("有多少了成员："+members.size());
		for (int i = 0; i < members.size(); i++) {
			
			String id =  NullUtil.checkNull(members.get(i)._id);
			String name = NullUtil.checkNull(members.get(i).displayName);
			String type = "member";
			
			DeptAndMember dam = new DeptAndMember();
			dam.setId(id);
			dam.setName(name);
			dam.setType(type);
			deptAndMembers.add(dam);
		}
		
		mListView = (ListView) findViewById(R.id.subdepartment_list);
//		System.out.println("deptAndMembers中有多少数据："+deptAndMembers.size());
		adapter = new SubDepartmentAdapter(SubDepartmentActivity.this, deptAndMembers);
		mListView.setAdapter(adapter);
		
		
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				DeptAndMember dam = (DeptAndMember) parent.getAdapter().getItem(position);
				if(dam.getType() == "dept") {
					//长按了部门，弹出对话框
					longClickDialog(dam);
				}
				return true;
			}
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DeptAndMember dam = (DeptAndMember) parent.getAdapter().getItem(position);
				if(dam.getType() == "dept") {
					//点击了部门
//					System.out.println("点击了部门");
					List<Department> departments = HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT));
					
					DepartmentParser dp = new DepartmentParser(departments);
					//该部门下有子部门或成员就可以进入下级界面
					if(dp.hasSubDepartment(dam.getId()) || dp.hasMembers(dam.getId())){
						//可以进入下级界面
						Intent intent = new Intent(SubDepartmentActivity.this, SubDepartmentActivity.class);
						intent.putExtra("departmentId", NullUtil.checkNull(dam.getId()));
						startActivity(intent);
					}
					
					
				}else if(dam.getType() == "member") {
					//点击了成员
//					System.out.println("点击了成员");
					Contacts contact = ContactsDao.getInstance().getContactById(dam.getId());
					Intent intent = new Intent(SubDepartmentActivity.this, ContactsDetailActivity.class);
					intent.putExtra("_id", NullUtil.checkNull(dam.getId()));
					intent.putExtra("otherName", NullUtil.checkNull(dam.getName()));
					intent.putExtra("contact", contact);
					startActivity(intent);
				}
			}
		});
	}

	private void setupTitleBar() {
		TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
		titlebar_name.setText("子部门");
		findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}

	public void longClickDialog(final DeptAndMember dam) {
		LayoutInflater myInflater = LayoutInflater.from(SubDepartmentActivity.this);
		final View myDialogView = myInflater.inflate(R.layout.department_dialog,
				null);
		final Builder dialog = new Builder(SubDepartmentActivity.this)
				.setView(myDialogView);
		alert = dialog.show();
		alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
		alert.getWindow().setGravity(Gravity.CENTER);

		// 添加子部门
		LinearLayout addDepartment = (LinearLayout) myDialogView
				.findViewById(R.id.department_add);
		addDepartment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert.dismiss();
				String product1 = user.product;
				if("zj".equals(NullUtil.checkNull(product1))) {
					boolean isAdmin = user.isAdmin;
					if(isAdmin) {
						Intent intent = new Intent(SubDepartmentActivity.this, DepartmentAddActivity.class);
						intent.putExtra("departmentId", dam.getId());
						startActivity(intent);
					}else {
						Toast.makeText(SubDepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
					}
				}else if("cc".equals(NullUtil.checkNull(product1))) {
					String type = user.type;
					if("manager".equals(NullUtil.checkNull(type))) {
						Intent intent = new Intent(SubDepartmentActivity.this, DepartmentAddActivity.class);
						intent.putExtra("departmentId", dam.getId());
						startActivity(intent);
					}else {
						Toast.makeText(SubDepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
					}
				}
				
			}
				
		});
		// 删除该部门
		LinearLayout deleteDepartment = (LinearLayout) myDialogView
				.findViewById(R.id.department_delete);
		
		deleteDepartment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert.dismiss();
				
				String product1 = user.product;
				if("zj".equals(NullUtil.checkNull(product1))) {
					boolean isAdmin = user.isAdmin;
					if(isAdmin) {
						final String id = dam.getId();
						List<Department> departments = HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT));
						DepartmentParser dp = new DepartmentParser(departments);
						if(!dp.hasSubDepartment(id)){
							//可以删除
							mMaterialDialog = new MaterialDialog(SubDepartmentActivity.this);
							mMaterialDialog.setTitle("温馨提示")
									.setMessage("确认删除该部门吗?")
									.setPositiveButton("确认", new View.OnClickListener() {
										@Override public void onClick(View v) {
											mMaterialDialog.dismiss();
											HttpManager.getInstance().deteleSubDepartment(InfoDao.getInstance().getConnectionId(), id, departmentId, new DeleteDepartmentResponseHandler());
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

						}else {
							Toast.makeText(SubDepartmentActivity.this, "该部门下有子部门，不可删除", Toast.LENGTH_SHORT).show();
						}
					}else {
						Toast.makeText(SubDepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
					}
				}else if("cc".equals(NullUtil.checkNull(product1))) {
					String type = user.type;
					if("manager".equals(NullUtil.checkNull(type))) {
						final String id = dam.getId();
						List<Department> departments = HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT));
						DepartmentParser dp = new DepartmentParser(departments);
						if(!dp.hasSubDepartment(id)){
							//可以删除
							mMaterialDialog = new MaterialDialog(SubDepartmentActivity.this);
							mMaterialDialog.setTitle("温馨提示")
									.setMessage("确认删除该部门吗?")
									.setPositiveButton("确认", new View.OnClickListener() {
										@Override public void onClick(View v) {
											mMaterialDialog.dismiss();
											HttpManager.getInstance().deteleSubDepartment(InfoDao.getInstance().getConnectionId(), id, departmentId, new DeleteDepartmentResponseHandler());
										}
									})
									.setNegativeButton("取消",
											new View.OnClickListener() {
												@Override public void onClick(View v) {
													mMaterialDialog.dismiss();
												}
											})
									.setCanceledOnTouchOutside(false)
									.show();				}else {
							Toast.makeText(SubDepartmentActivity.this, "该部门下有子部门，不可删除", Toast.LENGTH_SHORT).show();
						}
					}else {
						Toast.makeText(SubDepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
					}
				}
				
				
				
			}
		});
		// 更新该部门
		LinearLayout updateDepartment = (LinearLayout) myDialogView
				.findViewById(R.id.department_update);
		
		updateDepartment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alert.dismiss();
				
				String product1 = user.product;
				if("zj".equals(NullUtil.checkNull(product1))) {
					boolean isAdmin = user.isAdmin;
					if(isAdmin) {
						String id = dam.getId();
						Intent intent = new Intent(SubDepartmentActivity.this, DepartmentUpdateActivity.class);
						intent.putExtra("departmentId", id);
						startActivity(intent);
					}else {
						Toast.makeText(SubDepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
					}
				}else if("cc".equals(NullUtil.checkNull(product1))) {
					String type = user.type;
					if("manager".equals(type)) {
						String id = dam.getId();
						Intent intent = new Intent(SubDepartmentActivity.this, DepartmentUpdateActivity.class);
						intent.putExtra("departmentId", id);
						startActivity(intent);
					}else {
						Toast.makeText(SubDepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
					}
				}
				
				
				
			}
		});
	
	}
class DeleteDepartmentResponseHandler implements ResponseListener {
		

		@Override
		public void onFailed() {
			Toast.makeText(SubDepartmentActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSuccess(String responseString) {
//			System.out.println("删除子部门返回结果："+responseString);
			String message = HttpParser.getMessage(responseString);
			if (HttpParser.getSucceed(responseString)) {
				//删除成功了
				
				Toast.makeText(SubDepartmentActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent("department_update");
				sendBroadcast(intent);
				DepartmentActivityUtil.getInstance().exit();
				
				Intent it = new Intent(SubDepartmentActivity.this, DepartmentActivity.class);
				startActivity(it);
			} else {
				Toast.makeText(SubDepartmentActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		}
	}

}
