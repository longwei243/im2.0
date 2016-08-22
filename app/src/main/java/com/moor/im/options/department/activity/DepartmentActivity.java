package com.moor.im.options.department.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.MaterialDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.department.DepartmentActivityUtil;
import com.moor.im.options.department.adapter.DepartmentAdapter;
import com.moor.im.options.department.model.Department;
import com.moor.im.options.department.parser.DepartmentParser;
import com.moor.im.options.discussion.activity.CreateDiscussionActivity;

/**
 * 组织架构界面,该界面只显示根部门
 * @author LongWei
 *
 */
public class DepartmentActivity extends BaseActivity {

	SharedPreferences myPreferences;
	SharedPreferences.Editor myeditor;
	private UpdateDepartmentReceiver udr = new UpdateDepartmentReceiver();
	
	private ListView mListView;
	private List<Department> rootDepartments = new ArrayList<Department>();
	private List<Department> departments = new ArrayList<Department>();
	private DepartmentAdapter adapter;

	User user = UserDao.getInstance().getUser();

	MaterialDialog mMaterialDialog;
	ImageButton titlebar_add;
	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			case 0x1001:
				//重新更新数据从网络
//				System.out.println("部门handler接收到了更新界面的消息");
				getVersionFromNet();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DepartmentActivityUtil.getInstance().add(this);
		setContentView(R.layout.activity_department);
		myPreferences = getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		myeditor = myPreferences.edit();

		setupTitleBar();
		String product = user.product;
		if("zj".equals(NullUtil.checkNull(product))) {
			boolean isAdmin = user.isAdmin;
			if(!isAdmin) {
				titlebar_add.setVisibility(View.GONE);
			}
		}else if("cc".equals(NullUtil.checkNull(product))) {
			String type = user.type;
			if(!"manager".equals(NullUtil.checkNull(type))) {
				titlebar_add.setVisibility(View.GONE);
			}
		}


		mListView = (ListView) findViewById(R.id.department_list);
		getDepartmentDataFromNet();
		
		if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT) == null) {
			//从网络加载数据
			getDepartmentDataFromNet();
		}else {
			//从本地加载数据
			getDepartmentDataFromLocal();
		}
		
		getVersionFromNet();
		
		IntentFilter intentFilter = new IntentFilter("department_update");
		registerReceiver(udr, intentFilter);
		
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String rootId = NullUtil.checkNull(rootDepartments.get(position)._id);
				DepartmentParser dp = new DepartmentParser(departments);
				//该部门下有子部门或成员就可以进入下级界面
				if(dp.hasSubDepartment(rootId) || dp.hasMembers(rootId)){
					//可以进入下级界面
					Intent intent = new Intent(DepartmentActivity.this, SubDepartmentActivity.class);
					intent.putExtra("departmentId", rootId);
					startActivity(intent);
				}
			}
		});
	}
	private void setupTitleBar() {
		TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
		titlebar_name.setText("组织架构");
		findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		titlebar_add = (ImageButton) findViewById(R.id.titlebar_add);
		titlebar_add.setVisibility(View.VISIBLE);
		titlebar_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(DepartmentActivity.this, DepartmentAddActivity.class);
				startActivity(intent);
			}
		});

	}
	/**
	 * 从网络加载数据
	 */
	private void getDepartmentDataFromNet() {
		HttpManager.getInstance().getDepartments(InfoDao.getInstance().getConnectionId(),
				new GetDepartmentResponseHandler());
	}
	
	private void getDepartmentDataFromLocal() {
		rootDepartments.clear();
		departments.clear();
//		System.out.println("本地存的部门信息是："+MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT));
		if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT) != null) {
			departments = HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT));
			DepartmentParser dp = new DepartmentParser(departments);
			rootDepartments = dp.getRootDepartments();
			adapter = new DepartmentAdapter(DepartmentActivity.this, rootDepartments);
			mListView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

				@Override
				public void onCreateContextMenu(ContextMenu menu, View v,
												ContextMenuInfo menuInfo) {
					menu.add(0, 0, 0, "添加子部门");
					menu.add(0, 1, 0, "删除该部门");
					menu.add(0, 2, 0, "修改该部门");
				}
			});
		}
	}
	/**
	 * 获取部门信息返回处理器
	 * @author LongWei
	 *
	 */
	class GetDepartmentResponseHandler implements ResponseListener {

		@Override
		public void onFailed() {
//			Toast.makeText(DepartmentActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSuccess(String responseString) {
			if (HttpParser.getSucceed(responseString)) {
//				System.out.println("获取部门信息返回结果："+responseString);
				//将数据存到本地
				MobileApplication.cacheUtil.put(CacheKey.CACHE_DEPARTMENT, responseString);
				rootDepartments.clear();
				departments.clear();
				departments = HttpParser.getDepartments(responseString);
				DepartmentParser dp = new DepartmentParser(departments);
				rootDepartments = dp.getRootDepartments();
				adapter = new DepartmentAdapter(DepartmentActivity.this, rootDepartments);
				mListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						  menu.add(0, 0, 0, "添加子部门"); 
                          menu.add(0, 1, 0, "删除该部门"); 
                          menu.add(0, 2, 0, "修改该部门"); 
					}
				});
				
			} else {
//				Toast.makeText(DepartmentActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		 AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		
		switch(item.getItemId()) {
		case 0:
			
			String product = user.product;
			if("zj".equals(NullUtil.checkNull(product))) {
				boolean isAdmin = user.isAdmin;
				if(isAdmin) {
					//添加子部门
					Intent intent = new Intent(DepartmentActivity.this, DepartmentAddActivity.class);
					intent.putExtra("departmentId", rootDepartments.get(info.position)._id);
					startActivity(intent);
				}else {
					Toast.makeText(DepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
				}
			}else if("cc".equals(NullUtil.checkNull(product))) {
				String type = user.type;
				if("manager".equals(NullUtil.checkNull(type))) {
					//添加子部门
					Intent intent = new Intent(DepartmentActivity.this, DepartmentAddActivity.class);
					intent.putExtra("departmentId", rootDepartments.get(info.position)._id);
					startActivity(intent);
				}else {
					Toast.makeText(DepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case 1:
			
			String product1 = user.product;
			if("zj".equals(NullUtil.checkNull(product1))) {
				boolean isAdmin = user.isAdmin;
				if(isAdmin) {
					//删除子部门
					final String id = rootDepartments.get(info.position)._id;
					DepartmentParser dp = new DepartmentParser(departments);
					if(!dp.hasSubDepartment(id)){
						//可以删除
						mMaterialDialog = new MaterialDialog(this);
						mMaterialDialog.setTitle("温馨提示")
								.setMessage("确认删除该部门吗?")
								.setPositiveButton("确认", new View.OnClickListener() {
									@Override public void onClick(View v) {
										mMaterialDialog.dismiss();
										HttpManager.getInstance().deteleDepartment(InfoDao.getInstance().getConnectionId(), id, new DeleteDepartmentResponseHandler());
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
						Toast.makeText(DepartmentActivity.this, "该部门下有子部门，不可删除", Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(DepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
				}
			}else if("cc".equals(NullUtil.checkNull(product1))) {
				String type = user.type;
				if("manager".equals(NullUtil.checkNull(type))) {
					//删除子部门
					final String id = rootDepartments.get(info.position)._id;
					DepartmentParser dp = new DepartmentParser(departments);
					if(!dp.hasSubDepartment(id)){
						//可以删除
						mMaterialDialog = new MaterialDialog(this);
						mMaterialDialog.setTitle("温馨提示")
								.setMessage("确认删除该部门吗?")
								.setPositiveButton("确认", new View.OnClickListener() {
									@Override public void onClick(View v) {
										mMaterialDialog.dismiss();
										HttpManager.getInstance().deteleDepartment(InfoDao.getInstance().getConnectionId(), id, new DeleteDepartmentResponseHandler());
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
						Toast.makeText(DepartmentActivity.this, "该部门下有子部门，不可删除", Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(DepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case 2:
			//修改部门
			
			String product2 = user.product;
			if("zj".equals(NullUtil.checkNull(product2))) {
				boolean isAdmin = user.isAdmin;
				if(isAdmin) {
					String id = rootDepartments.get(info.position)._id;
					Intent intent = new Intent(DepartmentActivity.this, DepartmentUpdateActivity.class);
					intent.putExtra("departmentId", id);
					startActivity(intent);
				}else {
					Toast.makeText(DepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
				}
			}else if("cc".equals(NullUtil.checkNull(product2))) {
				String type = user.type;
				if("manager".equals(NullUtil.checkNull(type))) {
					String id = rootDepartments.get(info.position)._id;
					Intent intent = new Intent(DepartmentActivity.this, DepartmentUpdateActivity.class);
					intent.putExtra("departmentId", id);
					startActivity(intent);
				}else {
					Toast.makeText(DepartmentActivity.this, "您没有该权限", Toast.LENGTH_SHORT).show();
				}
			}
			
			break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	class DeleteDepartmentResponseHandler implements ResponseListener {
		

		@Override
		public void onFailed() {
//			Toast.makeText(DepartmentActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSuccess(String responseString) {
//			System.out.println("删除部门返回结果："+responseString);
			if (HttpParser.getSucceed(responseString)) {
				//删除成功了
				Toast.makeText(DepartmentActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessage(0x1001);
			} else {
			}
		}
	}
	
	
	class UpdateDepartmentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if("department_update".equals(intent.getAction())) {
//				System.out.println("接收到了部门更新的广播");
				handler.sendEmptyMessage(0x1001);
			}
		}
		
	}
	
	/**
	 * 从网络获取版本号
	 */
	public void getVersionFromNet() {
//		System.out.println("从网络获取版本号");
		HttpManager.getInstance().getVersion(InfoDao.getInstance().getConnectionId(),
				new GetVersionResponseHandler());
	}
	
	class GetVersionResponseHandler implements ResponseListener {

		@Override
		public void onFailed() {
//			Toast.makeText(DepartmentActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSuccess(String responseString) {
			if (HttpParser.getSucceed(responseString)) {
//				System.out.println("获取联系人版本号："+responseString);
				
				try {
					JSONObject jsonObject = new JSONObject(responseString);
					String DepartmentVersion = jsonObject.getString("DepartmentVersion");
					
					if(!"".equals(myPreferences.getString("DepartmentVersion", ""))) {
						if(!myPreferences.getString("DepartmentVersion", "").equals(NullUtil.checkNull(DepartmentVersion))) {
							//需更新
							getDepartmentDataFromNet();
						}else {
							getDepartmentDataFromLocal();
						}
					}
					
					if(DepartmentVersion != null && !"".equals(DepartmentVersion)) {
						myeditor.putString("DepartmentVersion", DepartmentVersion);
						myeditor.commit();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
//				Toast.makeText(DepartmentActivity.this, message, Toast.LENGTH_SHORT)
//						.show();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(udr);
	}
}
