package com.moor.im.common.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.Group;
import com.moor.im.common.model.User;

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
}
