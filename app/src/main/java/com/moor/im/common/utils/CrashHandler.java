package com.moor.im.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import com.csipsimple.api.SipProfile;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.MessageDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.db.dao.UserRoleDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.utils.log.LogUtil;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 */
public class CrashHandler implements UncaughtExceptionHandler {

	// private static final String TAG = "CrashHandler";
	private UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
	private static CrashHandler INSTANCE = new CrashHandler();// CrashHandler实例
	private Context context;// 程序的Context对象
	private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd_HH:mm:ss");// 用于格式化日期,作为日志文件名的一部分

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {

	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		this.context = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
		Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
	}

	/**
	 * 当UncaughtException发生时会转入该重写的方法来处理
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果自定义的没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(1000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
			} catch (InterruptedException e) {
				Exit();
			}
			// 退出程序
			Exit();
		}
	}

	private void Exit() {
		MobileApplication.getInstance().exit();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 如果处理了该异常信息;否则返回false.
	 */
	
	public boolean handleException(Throwable ex) {
		if (ex == null)
			return false;
		new  Thread() {
			public void run() {
				Looper.prepare();
				Toast.makeText(context, "很抱歉,程序出现异常,即将退出", Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		}.start();
		// 收集设备参数信息
		collectDeviceInfo(context);
		// 保存日志文件
		saveCrashInfo2File(ex);
		//判断次数，达到3次就清空应用数据
		checkClearData();
		return true;
	}

	private void checkClearData() {
		SharedPreferences mSp = context.getSharedPreferences(M7Constant.MAIN_SP, 0);
		SharedPreferences.Editor mEditor = mSp.edit();

		int count = mSp.getInt("causeCount", 0);
		if(count == 2) {
			mEditor.putBoolean(M7Constant.SP_LOGIN_SUCCEED ,false);
			mEditor.putInt("causeCount", 0);
			mEditor.commit();
			context.getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);
			MessageDao.getInstance().deleteAllMsgs();
			NewMessageDao.getInstance().deleteAllMsgs();
			UserDao.getInstance().deleteUser();
			UserRoleDao.getInstance().deleteUserRole();
			MobileApplication.cacheUtil.clear();
		}else {
			mEditor.putInt("causeCount", mSp.getInt("causeCount", 0)+1);
			mEditor.commit();
		}
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param context
	 */
	public void collectDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();// 获得包管理器
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				info.put("versionName", versionName);
				info.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Field[] fields = Build.class.getDeclaredFields();// 反射机制
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				info.put(field.getName(), field.get("").toString());
				// Log.d(TAG, field.getName() + ":" + field.get(""));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : info.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		Throwable cause = ex.getCause();
		// 循环着把所有的异常信息写入writer中
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();// 记得关闭
		String result = writer.toString();
		LogUtil.d("异常信息是:"+result);
//		sb.append(result);

		long timetamp = System.currentTimeMillis();
		String time = format.format(new Date());
		//上传服务器
		if(Utils.isNetWorkConnected(context)) {
			HttpManager.getInstance().sendErrorLog(InfoDao.getInstance().getConnectionId(), time, sb.toString(), result, null);
		}
		// 保存文件
//		String fileName = "error-" + time + "-" + timetamp + ".log";
//		if (Environment.getExternalStorageState().equals(
//				Environment.MEDIA_MOUNTED)) {
//			try {
//				File dir = new File(Environment.getExternalStorageDirectory()
//						.getAbsolutePath()
//						+ File.separator
//						+ "m7"
//						+ File.separator + "Log");
//				if (!dir.exists()) {
//					dir.mkdir();
//				}
//				FileOutputStream fos = new FileOutputStream(new File(dir,
//						fileName));
//				fos.write(sb.toString().getBytes());
//				fos.close();
//				return fileName;
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		return null;
	}
}