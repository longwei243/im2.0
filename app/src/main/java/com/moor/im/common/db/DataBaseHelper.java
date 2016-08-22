package com.moor.im.common.db;

import java.sql.SQLException;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.moor.imkf.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.moor.imkf.ormlite.dao.Dao;
import com.moor.imkf.ormlite.support.ConnectionSource;
import com.moor.imkf.ormlite.table.TableUtils;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.Info;
import com.moor.im.common.model.NewMessage;
import com.moor.im.common.model.User;
import com.moor.im.common.model.UserRole;


/**
 * 操作数据库的帮助类，使用了OrmLite框架
 * 
 * @author LongWei
 * 
 */
public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "qmoor.db";
	private static final int DATABASE_VERSION = 10;
	private Dao<User, Integer> userDao = null;
	private Dao<UserRole, Integer> userRoleDao = null;
	private Dao<Info, Integer> infoDao = null;
	private Dao<Contacts, Integer> contactDao = null;
	private Dao<FromToMessage, Integer> messageDao = null;
	private Dao<NewMessage, Integer> newMessageDao = null;

	private DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, UserRole.class);
			TableUtils.createTable(connectionSource, Info.class);
			TableUtils.createTable(connectionSource, Contacts.class);
			TableUtils.createTable(connectionSource, FromToMessage.class);
			TableUtils.createTable(connectionSource, NewMessage.class);
			// contactsDao = getContactsDao();
			userDao = getUserDao();
			userRoleDao = getUserRoleDao();
			infoDao = getInfoDao();
			contactDao = getContactsDao();
			messageDao = getMessageDao();
			newMessageDao = getNewMessageDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, UserRole.class, true);
			TableUtils.dropTable(connectionSource, Info.class, true);
			TableUtils.dropTable(connectionSource, Contacts.class, true);
			TableUtils.dropTable(connectionSource, FromToMessage.class, true);
			TableUtils.dropTable(connectionSource, NewMessage.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取用户信息
	 *
	 * @return
	 * @throws SQLException
	 */
	public Dao<User, Integer> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}
		return userDao;
	}

	public Dao<UserRole, Integer> getUserRoleDao() throws SQLException {
		if (userRoleDao == null) {
			userRoleDao = getDao(UserRole.class);
		}
		return userRoleDao;
	}

	public Dao<Info, Integer> getInfoDao() throws SQLException {
		if (infoDao == null) {
			infoDao = getDao(Info.class);
		}
		return infoDao;
	}
	public Dao<Contacts, Integer> getContactsDao() throws SQLException {
		if (contactDao == null) {
			contactDao = getDao(Contacts.class);
		}
		return contactDao;
	}
	public Dao<FromToMessage, Integer> getMessageDao() throws SQLException {
		if (messageDao == null) {
			messageDao = getDao(FromToMessage.class);
		}
		return messageDao;
	}
	public Dao<NewMessage, Integer> getNewMessageDao() throws SQLException {
		if (newMessageDao == null) {
			newMessageDao = getDao(NewMessage.class);
		}
		return newMessageDao;
	}
	private static DataBaseHelper instance;

	/**
	 * 单例获取该Helper
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized DataBaseHelper getHelper(Context context) {
		if (instance == null) {
			synchronized (DataBaseHelper.class) {
				if (instance == null)
					instance = new DataBaseHelper(context);
			}
		}
		return instance;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
		userDao = null;
		userRoleDao = null;
		infoDao = null;
		contactDao = null;
		messageDao = null;
		newMessageDao = null;
	}
}
