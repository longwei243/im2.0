package com.moor.im.common.db.dao;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.db.DataBaseHelper;
import com.moor.im.common.model.User;

import android.content.Context;

/**
 * 用户dao方法
 *
 */
public class UserDao {
	private Dao<User, Integer> userDao = null;
	private DataBaseHelper helper = DataBaseHelper.getHelper(MobileApplication
			.getInstance());
	private static UserDao instance;

	private UserDao() {
		try {
			userDao = helper.getDao(User.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static UserDao getInstance() {
		if (instance == null) {
			synchronized (UserDao.class) {
				if(instance == null) {
					instance = new UserDao();
				}
			}
		}
		return instance;
	}

	/**
	 * 存用户信息
	 * 
	 * @param user
	 */
	public void insertUser(User user) {
		try {
			userDao.create(user);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取用户
	 * @return
	 */
	public User getUser() {
		User user = new User();
		try {
			if(userDao.queryForAll() != null && userDao.queryForAll().size() > 0) {
				user = userDao.queryForAll().get(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * 更新用户
	 */
	public void updateUser(User user) {
		try {
			User u = userDao.queryBuilder().where().eq("_id", user._id).query().get(0);
			u = user;
			userDao.update(u);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除用户信息
	 */
	public void deleteUser() {
		try {
			userDao.delete(userDao.queryForAll());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
