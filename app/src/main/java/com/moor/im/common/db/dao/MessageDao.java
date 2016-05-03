package com.moor.im.common.db.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.db.DataBaseHelper;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.utils.log.LogUtil;

/**
 * 所有消息的dao方法
 */
public class MessageDao {
	private Dao<FromToMessage, Integer> fromToMessageDao = null;
	private DataBaseHelper helper = DataBaseHelper.getHelper(MobileApplication
			.getInstance());
	private static MessageDao instance;

	private MessageDao() {
		try {
			fromToMessageDao = helper.getDao(FromToMessage.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static MessageDao getInstance() {
		if (instance == null) {
			instance = new MessageDao();
		}
		return instance;
	}

	/**
	 * 删除所有消息
	 */
	public void deleteAllMsgs() {
		try {
			fromToMessageDao.delete(fromToMessageDao.queryForAll());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取已经上次获取到消息的id
	 * 
	 * @param context
	 * @return
	 */
	public ArrayList<String> getUnReadDao() {
		List<FromToMessage> list = null;
		ArrayList<String> array = new ArrayList<String>();
		try {
			list = fromToMessageDao.queryBuilder().where().eq("unread", "1")
					.query();
			for (int i = 0; i < list.size(); i++) {
				array.add(list.get(i)._id);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}

	/**
	 * 把上一次取的消息标为已取 1未取，0已取
	 **/
	public void updateMsgsIdDao() {
		List<FromToMessage> msgs = new ArrayList<FromToMessage>();
		try {
			msgs = fromToMessageDao.queryBuilder().where().eq("unread", "1")
					.query();
			for (int i = 0; i < msgs.size(); i++) {
				msgs.get(i).unread = "0";
				fromToMessageDao.update(msgs.get(i));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 插入服务器获取的消息
	 *
	 */
	public void insertGetMsgsToDao(List<FromToMessage> fromToMessage) {
		for (int i = 0; i < fromToMessage.size(); i++) {
			try {
				fromToMessage.get(i).unread = "1";
				fromToMessage.get(i).userType = "1";
				fromToMessage.get(i).sendState = "true";
				fromToMessageDao.create(fromToMessage.get(i));

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 把发送的消息存到数据库中
	 * @param fromToMessage
	 */
	public void insertSendMsgsToDao(FromToMessage fromToMessage) {

		fromToMessage._id = UUID.randomUUID().toString();
		try {
			fromToMessageDao.create(fromToMessage);
			LogUtil.d("MessageDao", "新消息插入到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新数据库中的消息
	 * @param message
	 */

	public void updateMsgToDao(FromToMessage message) {
		//先将该消息从数据库中查出来
		try {
			FromToMessage msg = fromToMessageDao.queryBuilder().where().eq("_id", message._id).query().get(0);
			//更新该数据
			msg = message;
			fromToMessageDao.update(msg);
			LogUtil.i("fromToMessageDao", "消息更新到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 便捷方法，更新数据库，该消息发送成功了
	 * @param message
	 */
	public void updateSucceedMsgToDao(FromToMessage message) {
		//先将该消息从数据库中查出来
		try {
			FromToMessage msg = fromToMessageDao.queryBuilder().where().eq("_id", message._id).query().get(0);
			msg.sendState = "true";
			//更新该数据
			fromToMessageDao.update(msg);
			LogUtil.i("fromToMessageDao", "消息发送成功更新到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 便捷方法，更新数据库，该消息发送失败
	 * @param message
	 */
	public void updateFailedMsgToDao(FromToMessage message) {
		//先将该消息从数据库中查出来
		try {
			FromToMessage msg = fromToMessageDao.queryBuilder().where().eq("_id", message._id).query().get(0);
			msg.sendState = "false";
			//更新该数据
			fromToMessageDao.update(msg);
			LogUtil.i("fromToMessageDao", "消息发送失败更新到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 按对方的id取消息，聊天界面中使用
	 * 
	 * @param _id
	 * @return
	 */
	public List<FromToMessage> getOneMessage(String _id, int i) {
		List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
		try {
			fromToMessage = fromToMessageDao.queryBuilder()
					.orderBy("when", false).limit(i * 15).where().eq("sessionId", _id)
					.query();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fromToMessage;
	}

	/**
	 * 查最新一条的消息
	 * 
	 * @param _id
	 * @return
	 */
	public List<FromToMessage> getFirstMessage(String _id) {
		List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
		try {
			fromToMessage = fromToMessageDao.queryBuilder()
					.orderBy("id", false).limit(1).where().eq("from", _id)
					.query();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fromToMessage;
	}

	/**
	 * 是否查到最底端
	 * 
	 * @param i
	 * @param _id
	 * @return
	 */
	public Boolean isReachEndMessage(int i, String _id) {
		List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
		Boolean flag = false;
		try {
			fromToMessage = fromToMessageDao.queryBuilder().where()
					.eq("sessionId", _id).query();
			if (i >= fromToMessage.size()) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 获取所有已经获取的消息id
	 * @return
	 */
	public ArrayList<String> getAllReceivedMsg() {
		List<FromToMessage> list = null;
		ArrayList<String> array = new ArrayList<String>();
		try {
			list = fromToMessageDao.queryForAll();
			for (int i = 0; i < list.size(); i++) {
				array.add(list.get(i)._id);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}

	/**
	 * 检查获取到的消息是否已经存在了
	 * @param fromToMessage
	 * @return
	 */
	public boolean contains(List<FromToMessage> fromToMessage) {
		ArrayList<String> array = getAllReceivedMsg();
		for (int i=0; i<fromToMessage.size(); i++) {
			for (int j=0; j<array.size(); j++) {
				if(fromToMessage.get(i)._id.equals(array.get(j))) {
					return true;
				}
			}
		}
		return false;
	}
}
