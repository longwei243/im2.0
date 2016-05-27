package com.moor.im.options.dial;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;

/**
 * 正在呼叫
 */
public class CallingActivity extends Activity {

	private String userName;
	private TextView mCallingNumber;

	PhoneReceiver phoneReceiver;

	User user = UserDao.getInstance().getUser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_calling);
		Intent intent = getIntent();
		userName = intent.getStringExtra("phone_number");
		init();
		dialBack(userName);

	}

	// 初始化方法
	public void init() {
		mCallingNumber = (TextView) findViewById(R.id.calling_number);
		mCallingNumber.setText(userName);

		phoneReceiver = new PhoneReceiver();
		IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
		registerReceiver(phoneReceiver, filter);

	}

	private void dialBack(final String otherNum) {
		String serverIp = user.pbxSipAddr;
		String servierIpStr = serverIp.split(":")[0];
		String actionId = new Random().nextInt() * 10000 + "";
		String account = user.account;
		String exten = user.exten;
		String pbx = user.pbx;
		
		String urlStr = "http://"+servierIpStr+"/app?Action=Dialout&ActionID="+actionId+"&Account="+account+"&Exten="+otherNum+"&FromExten="+exten+"&PBX="+pbx+"&ExtenType=Local";

		HttpManager.getInstance().get(urlStr, new ResponseListener(){
			@Override
			public void onFailed() {
				Toast.makeText(CallingActivity.this, "请检查您的网络问题",
						Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onSuccess(String responseStr) {
				try {
					JSONObject jb = new JSONObject(responseStr);
					boolean succeed = jb.getBoolean("Succeed");
					if(!succeed) {
						Toast.makeText(CallingActivity.this, "呼叫失败",
								Toast.LENGTH_LONG).show();
						finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(CallingActivity.this, "呼叫失败",
							Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});
	}

	class PhoneReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 如果是去电
			if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				String phoneNumber = intent
						.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			} else {
				// 查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电.
				// 如果我们想要监听电话的拨打状况，需要这么几步 :
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
				// 设置一个监听器

			}
		}

		PhoneStateListener listener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				// 注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
				super.onCallStateChanged(state, incomingNumber);
				switch (state) {
					case TelephonyManager.CALL_STATE_IDLE:
						// System.out.println("挂断");
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						// System.out.println("接听");

						break;
					case TelephonyManager.CALL_STATE_RINGING:
						// System.out.println("响铃:来电号码" + incomingNumber);
						finish();
						break;
				}
			}
		};
	}

	/**
	 * 自动接听
	 */
	private void answerRingingCall() {

		try {
			// 放开耳机按钮
			Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_HEADSETHOOK);
			localIntent3.putExtra("android.intent.extra.KEY_EVENT",
					localKeyEvent2);
			sendOrderedBroadcast(localIntent3,
					"android.permission.CALL_PRIVILEGED");

			// 插耳机
			Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
			localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			localIntent1.putExtra("state", 1);
			localIntent1.putExtra("microphone", 1);
			localIntent1.putExtra("name", "Headset");
			sendOrderedBroadcast(localIntent1,
					"android.permission.CALL_PRIVILEGED");
			// 按下耳机按钮
			Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_HEADSETHOOK);
			localIntent2.putExtra("android.intent.extra.KEY_EVENT",
					localKeyEvent1);
			sendOrderedBroadcast(localIntent2,
					"android.permission.CALL_PRIVILEGED");
			// 放开耳机按钮
			localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
			localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_HEADSETHOOK);
			localIntent3.putExtra("android.intent.extra.KEY_EVENT",
					localKeyEvent2);
			sendOrderedBroadcast(localIntent3,
					"android.permission.CALL_PRIVILEGED");
			// 拔出耳机
			Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
			localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			localIntent4.putExtra("state", 0);
			localIntent4.putExtra("microphone", 1);
			localIntent4.putExtra("name", "Headset");
			sendOrderedBroadcast(localIntent4,
					"android.permission.CALL_PRIVILEGED");
		} catch (Exception e) {
			Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);  
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);  
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);  
			CallingActivity.this.sendOrderedBroadcast(meidaButtonIntent, null);

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(phoneReceiver);
	}
}
