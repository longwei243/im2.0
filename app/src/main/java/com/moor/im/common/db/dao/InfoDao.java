package com.moor.im.common.db.dao;

import com.j256.ormlite.dao.Dao;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.db.DataBaseHelper;
import com.moor.im.common.model.Info;

import java.sql.SQLException;

/**
 * Created by longwei on 2016/4/11.
 */
public class InfoDao {

    private Dao<Info, Integer> infoDao = null;
    private DataBaseHelper helper = DataBaseHelper.getHelper(MobileApplication.getInstance());
    private static InfoDao instance;

    private InfoDao() {
        try {
            infoDao = helper.getDao(Info.class);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static InfoDao getInstance() {
        if (instance == null) {
            synchronized (InfoDao.class) {
                if(instance == null) {
                    instance = new InfoDao();
                }
            }

        }
        return instance;
    }

    /**
     * 将信息存入数据库中
     * @param info
     */
    public void insertInfoToDao(Info info) {
        try {
            infoDao.delete(infoDao.queryForAll());
            infoDao.create(info);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从数据库中获取Info
     * @return
     */
    public Info getInfo() {
        Info info = null;
        try {
            info = infoDao.queryForAll().get(0);
            if(info != null) {
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存connectionId
     * @param connectionId
     */
    public void saveConnectionId(String connectionId) {
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                info.connectionId = connectionId;
                infoDao.update(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从数据库中获取connectionId
     * @return
     */
    public String getConnectionId() {
        String connectionId = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                connectionId = info.connectionId;
                return connectionId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从数据库中获取loginName
     * @return
     */
    public String getLoginName() {
        String loginName = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                loginName = info.name;
                return loginName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    public void saveIsChangePW(String isChangePw) {
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                info.isChangePassWord = isChangePw;
                infoDao.update(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getIsChangePW() {
        String isChangePw = "false";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                isChangePw = info.isChangePassWord;
                return isChangePw;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 删除所有数据
     */
    public void deleteAll() {
        try {
            infoDao.delete(infoDao.queryForAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setLoginStateToSuccess() {
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                info.isSucceed = "true";
                infoDao.update(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
