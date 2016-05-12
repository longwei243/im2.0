package com.csipsimple;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.MediaState;
import com.csipsimple.api.SipCallSession;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.csipsimple.service.SipService;

/**
 * sip电话管理类
 * @author longwei
 *
 */
public class SipCallManager {

	private Context appContext;
	
	private static SipCallManager instance = null;
	
	private boolean isCreated = false;
	
	private SipCallManager() {}
	
	public static SipCallManager getInstance() {
		if(instance == null) {
			synchronized (SipCallManager.class) {
				if(instance == null) {
					instance = new SipCallManager();
				}
			}
		}
		
		return instance;
	}
	private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

       @Override
       public void onServiceConnected(ComponentName arg0, IBinder arg1) {
           service = ISipService.Stub.asInterface(arg1);
           isCreated = true;
       }

       @Override
       public void onServiceDisconnected(ComponentName arg0) {
           service = null;
           isCreated = false;
       }
   };
	/**
	 * 初始化sipservice
	 * @param context
	 */
	public void init(Context context, OnSipInitListener listener) {
		this.appContext = context.getApplicationContext();
		
		//启动sipService
		Intent serviceIntent = new Intent(appContext, SipService.class);
        appContext.startService(serviceIntent);
        
        boolean succeed = appContext.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        if(succeed) {
        	if(listener != null) {
        		listener.onSuccess();
        	}
        }else {
        	if(listener != null) {
        		listener.onFailed();
        	}
        }
	}
	
	/**
	 * 创建sip账户
	 * @param sipurl sip服务器地址
	 * @param username 用户名
	 * @param password 密码
	 * @return
	 */
	public SipProfile createAccount(String name, String sipurl, String username, String password) {
		SipProfile account = new SipProfile();
		
		account.display_name = "name";
		if(sipurl != null && username != null && password != null) {
			String[] serverParts = sipurl.split(":");
			account.acc_id = "<sip:" + SipUri.encodeUser(username) + "@"+serverParts[0].trim()+">";
			
			String regUri = "sip:" + sipurl;
			account.reg_uri = regUri;
			account.proxies = new String[] { regUri } ;


			account.realm = "*";
			account.username = username;
			account.data = password;
			account.scheme = SipProfile.CRED_SCHEME_DIGEST;
			account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
			//By default auto transport
			account.transport = SipProfile.TRANSPORT_UDP;
			Uri uri = appContext.getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());
		}
		
		return account;

	} 
	
	/**
	 * 删除所有sip用户
	 */
	public void deleteAllAccout() {
		appContext.getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);
	}
	
	/**
	 * 注册已存在的账户
	 */
	public void registerAccount() {
		Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
		appContext.sendBroadcast(intent);
	}
	
	/**
	 * 拨打电话
	 * @param num
	 * @param accId
	 */
	public void makeCall(String num, int accId) {
		if(!isCreated) {
			return;
		}
		try {
			service.makeCall(num, accId);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 接听电话
	 */
	public void answer(int callId) {
		if(!isCreated) {
			return;
		}
		try {
			service.answer(callId, SipCallSession.StatusCode.OK);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 挂起电话
	 */
	public void hold(int callId) {
		if(!isCreated) {
			return;
		}
		try {
			service.hold(callId);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 挂起电话后重新通话
	 */
	public void reinvite(int callId) {
		if(!isCreated) {
			return;
		}
		try {
			service.reinvite(callId, true);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 挂断电话
	 */
	public void hangup(int callId) {
		if(!isCreated) {
			
			return;
		}
		try {
			System.out.println("执行了挂断电话的方法");
			service.hangup(callId, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 拒绝来电
	 */
	public void rejectCall(int callId) {
		if(!isCreated) {
			return;
		}
		try {
			service.hangup(callId, 486);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送字符(0-9,*,#)
	 */
	public void sendDtmf(int callId, int keyCode) {
		if(!isCreated) {
			return;
		}
		try {
			service.sendDtmf(callId, keyCode);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置静音
	 * @param state
	 */
	public void setMicrophoneMute(boolean state) {
		if(!isCreated) {
			return;
		}
		try {
			service.setMicrophoneMute(state);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置外放
	 * @param state
	 */
	public void setSpeakerphoneOn(boolean state) {
		if(!isCreated) {
			return;
		}
		try {
			service.setSpeakerphoneOn(state);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取当前媒体状态
	 * @param state
	 */
	public MediaState getCurrentMediaState() {
		if(!isCreated) {
			return null;
		}
		MediaState ms = null;
		try {
			ms = service.getCurrentMediaState();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ms;
	}
	
	public SipCallSession getCallInfo(int callId) {
		if(!isCreated) {
			return null;
		}
		try {
			SipCallSession scs = service.getCallInfo(callId);
			return scs;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取当前所有通话
	 * @return
	 */
	public SipCallSession[] getCalls() {
		if(!isCreated) {
			return null;
		}
		try {
			SipCallSession[] scs = service.getCalls();
			return scs;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SipCallSession[0];
	} 
	
	/**
	 * 获取当前激活状态通话的信息
	 * @return
	 */
	public SipCallSession getCurrentCallInfo() {
		SipCallSession currentCallInfo = null;
		SipCallSession[] callsInfo = getCalls();
        if (callsInfo == null) {
            return null;
        }
        for (SipCallSession callInfo : callsInfo) {
            currentCallInfo = getPrioritaryCall(callInfo, currentCallInfo);
        }
        return currentCallInfo;
	}
	
	/**
	 * 获得优先级高的通话
	 * @param call1
	 * @param call2
	 * @return
	 */
	private SipCallSession getPrioritaryCall(SipCallSession call1, SipCallSession call2) {
        // We prefer the not null
        if (call1 == null) {
            return call2;
        } else if (call2 == null) {
            return call1;
        }
        // We prefer the one not terminated
        if (call1.isAfterEnded()) {
            return call2;
        } else if (call2.isAfterEnded()) {
            return call1;
        }
        // We prefer the one not held
        if (call1.isLocalHeld()) {
            return call2;
        } else if (call2.isLocalHeld()) {
            return call1;
        }
        // We prefer the older call 
        // to keep consistancy on what will be replied if new call arrives
        return (call1.getCallStart() > call2.getCallStart()) ? call2 : call1;
    }
	
	/**
	 * 销毁操作
	 */
	public void destory() {
		appContext.unbindService(connection);
		isCreated = false;
	}

}
