package com.moor.im.common.http;

import com.moor.im.options.mobileassistant.customer.model.CustomerHistoryData;
import com.moor.im.options.mobileassistant.model.MACustomer;
import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.Group;
import com.moor.im.common.model.User;
import com.moor.im.options.department.model.Department;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by longwei on 2016/4/11.
 */
public class HttpParser {

    /**
     * 获取返回成功状态
     *
     * @param responseString
     * @returnF
     */
    public static boolean getSucceed(String responseString) {
        boolean succeed = false;
        try {
            JSONObject o = new JSONObject(responseString);
            succeed = o.getBoolean("Succeed");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return succeed;
    }

    /**
     * 手机助手
     * @param responseString
     * @return
     */
    public static boolean getSuccess(String responseString) {
        boolean succeed = false;
        try {
            JSONObject o = new JSONObject(responseString);
            succeed = o.getBoolean("success");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return succeed;
    }

    /**
     * 获取返回消息
     *
     * @param responseString
     * @return
     */
    public static String getMessage(String responseString) {
        String message = "";
        try {
            JSONObject o = new JSONObject(responseString);
            message = o.getString("Message");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }
    /**
     * 获取返回错误码
     *
     * @param responseString
     * @return
     */
    public static String getErrorCode(String responseString) {
        String message = "";
        try {
            JSONObject o = new JSONObject(responseString);
            message = o.getString("ErrorCode");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }
    /**
     * 获取大量消息的id
     *
     * @param responseString
     * @return
     */
    public static String getLargeMsgId(String responseString) {
        String largeMsgId = "";
        try {
            JSONObject o = new JSONObject(responseString);
            largeMsgId = o.getString("LargeMsgId");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return largeMsgId;
    }

    /**
     * 用户信息
     *
     * @param responseString
     * @return
     */
    public static User getUserInfo(String responseString) {
        User user = new User();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONObject o1 = o.getJSONObject("UserInfo");
            Gson gson = new Gson();
            user = gson.fromJson(o1.toString(), User.class);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return user;
    }

    /**
     * 用户权限列表
     *
     * @param responseString
     * @return
     */
    public static List<String> getLimitInList(String responseString) {
        List<String> list = new ArrayList<>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONObject o1 = o.getJSONObject("Authority");
            JSONArray array = o1.getJSONArray("limit_in");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取联系人列表
     */
    public static List<Contacts> getContacts(String responseString) {
        List<Contacts> contacts = new ArrayList<Contacts>();

        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("Contacts");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            contacts = gson.fromJson(o1.toString(),
                    new TypeToken<List<Contacts>>() {
                    }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return contacts;
    }

    /**
     * 取消息
     *
     * @param responseString
     * @return
     */
    public static List<FromToMessage> getMsgs(String responseString) {
        List<FromToMessage> newMessage = new ArrayList<FromToMessage>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("Msgs");
            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            newMessage = gson.fromJson(o1.toString(),
                    new TypeToken<List<FromToMessage>>() {
                    }.getType());
            //收到的新消息逆序
            Collections.reverse(newMessage);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return newMessage;
    }
    /**
     * 是否有大量的消息
     *
     * @param responseString
     * @return
     */
    public static boolean isLargeMsg(String responseString) {
        try {
            JSONObject o = new JSONObject(responseString);
            boolean isLargeMsg = o.getBoolean("HasLargeMsgs");
            if(isLargeMsg) {
                return true;
            }

        } catch (JSONException e) {
            return false;
        }

        return false;
    }
    /**
     * 是否还有大量的消息
     *
     * @param responseString
     * @return
     */
    public static boolean hasMoreMsgs(String responseString) {
        try {
            JSONObject o = new JSONObject(responseString);
            boolean isHasMore = o.getBoolean("HasMore");
            if(isHasMore) {
                return true;
            }

        } catch (JSONException e) {
            return false;
        }

        return false;
    }

    /**
     * 获取uptoken
     *
     * @param responseString
     * @return
     */
    public static String getUpToken(String responseString) {
        String s = "";
        try {
            JSONObject o = new JSONObject(responseString);
            s = o.getString("uptoken");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;

    }

    /**
     * 获取群组列表
     */
    public static List<Group> getGroups(String responseString) {
        List<Group> groups = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("Groups");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            groups = gson.fromJson(o1.toString(),
                    new TypeToken<List<Group>>() {
                    }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return groups;
    }
    /**
     * 有网络数据获取讨论组
     *
     * @param responseString
     * @return
     */
    public static List<Discussion> getDiscussion(String responseString) {
        if(responseString == null || "".equals(responseString)) {
            return null;
        }
        List<Discussion> discussions = new ArrayList<Discussion>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("Discussions");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            discussions = gson.fromJson(o1.toString(),
                    new TypeToken<List<Discussion>>() {
                    }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return discussions;
    }

    /**
     * 获取一个部门
     *
     * @param responseString
     * @return
     */
    public static Department getDepartmentInfo(String responseString) {
        Department department = new Department();

        try {

            JSONObject o = new JSONObject(responseString);
            JSONObject o1 = o.getJSONObject("Department");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            department = gson.fromJson(o1.toString(), new TypeToken<Department>() {
            }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return department;
    }

    /**
     * 有网络数据获取组织结构
     *
     * @param responseString
     * @return
     */
    public static List<Department> getDepartments(String responseString) {
        List<Department> departments = new ArrayList<Department>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("Departments");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            departments = gson.fromJson(o1.toString(),
                    new TypeToken<List<Department>>() {
                    }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return departments;
    }

    public static List<MACustomer> getCustomers(String responseString) {
        List<MACustomer> customers = new ArrayList<MACustomer>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("list");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            customers = gson.fromJson(o1.toString(),
                    new TypeToken<List<MACustomer>>() {
                    }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return customers;
    }

    public static List<CustomerHistoryData> getCustomerHistoryData(String responseString) {
        List<CustomerHistoryData> chd = new ArrayList<CustomerHistoryData>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONObject jb = o.getJSONObject("data");
            JSONArray o1 = jb.getJSONArray("list");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            chd = gson.fromJson(o1.toString(),
                    new TypeToken<List<CustomerHistoryData>>() {
                    }.getType());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return chd;
    }
}
