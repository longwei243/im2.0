package com.moor.im.common.http;

import android.os.Handler;
import android.os.Looper;

import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.Group;
import com.moor.im.common.utils.JSONWriter;
import com.moor.im.common.utils.Utils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.imkf.okhttp.Call;
import com.moor.imkf.okhttp.Callback;
import com.moor.imkf.okhttp.FormEncodingBuilder;
import com.moor.imkf.okhttp.OkHttpClient;
import com.moor.imkf.okhttp.Request;
import com.moor.imkf.okhttp.RequestBody;
import com.moor.imkf.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by longwei on 2016/4/11.
 */
public class HttpManager {

    private static HttpManager instance;
    public OkHttpClient okHttpClient;
    private Handler mDelivery;

    private HttpManager() {
        mDelivery = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient();
    }

    public static HttpManager getInstance() {
        if(instance == null) {
            synchronized (HttpManager.class) {
                if(instance == null) {
                    instance = new HttpManager();
                }
            }
        }
        return instance;
    }

    public void get(String url, final ResponseListener listener) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(listener != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });

                }
            }
            @Override
            public void onResponse(Response response){
                if(listener != null) {
                    final String responseStr;
                    try {
                        responseStr = response.body().string();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(responseStr);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendPost(String content, final ResponseListener listener) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("data", content)
                .build();
        Request request = new Request.Builder()
                .url(RequestUrl.baseHttp1)
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(listener != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });

                }
            }
            @Override
            public void onResponse(Response response){
                if(listener != null) {
                    final String responseStr;
                    try {
                        responseStr = response.body().string();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(responseStr);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendPostForMobileAssistant(String content, final ResponseListener listener) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("data", content)
                .build();
        Request request = new Request.Builder()
                .url(RequestUrl.baseHttpMobile)
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(listener != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });

                }
            }
            @Override
            public void onResponse(Response response){
                if(listener != null) {
                    final String responseStr;
                    try {
                        responseStr = response.body().string();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(responseStr);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取用户信息
     * @param connectionId
     * @param listener
     */
    public void getUserInfo(String connectionId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "getUserInfoWithAuthority");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     *发送错误日志
     * @param connectionId
     * @param time
     * @param log
     */
    public void sendErrorLog(String connectionId, String time, String log, String cause, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "sendErrorLog");
            json.put("Time", time);
            json.put("Log", log);
            json.put("Cause", cause);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 注销
     * @param connectionId
     * @param listener
     */
    public void loginOff(String connectionId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "logoff");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 获取版本号
     * @param connectionId
     * @param listener
     */
    public void getVersion(String connectionId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "getDepartmentAndContactsVersion");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 获取7牛token
     * @param connectionId
     * @param listener
     */
    public void getQiNiuToken(String connectionId, String fileName, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "qiniu.getUptoken");
            json.put("fileName", fileName);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 获取部门
     * @param connectionId
     * @param listener
     */
    public void getDepartments(String connectionId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "getDepartments");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 获取部门
     * @param connectionId
     * @param listener
     */
    public void deteleDepartment(String connectionId, String id, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("_id", id);
            json.put("Action", "delDepartment");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 添加部门
     * @param connectionId
     * @param members
     * @param subDept
     * @param name
     * @param desc
     * @param root
     * @param listener
     */
    public void addDepartment(String connectionId, ArrayList members,
                              ArrayList subDept, String name, String desc, boolean root, final ResponseListener listener) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ConnectionId", Utils.replaceBlank(connectionId));
        map.put("Members", members);
        map.put("Subdepartments", subDept);
        map.put("Name", name);
        map.put("Description", desc);
        map.put("Root", root);
        map.put("Action", "addDepartment");
        JSONWriter jw = new JSONWriter();
        String content = jw.write(map);
        sendPost(content, listener);
    }

    public void updateDepartment(String connectionId, String _id, ArrayList members,
                                 ArrayList subDept, String name, String desc, boolean root, final ResponseListener listener) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ConnectionId", Utils.replaceBlank(connectionId));
        map.put("_id", _id);
        map.put("Members", members);
        map.put("Subdepartments", subDept);
        map.put("Name", name);
        map.put("Description", desc);
        map.put("Root", root);
        map.put("Action", "updateDepartment");
        JSONWriter jw = new JSONWriter();
        String content = jw.write(map);
        sendPost(content, listener);
    }

    public void deteleSubDepartment(String connectionId, String id, String parentId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("_id", id);
            json.put("ParentId", parentId);
            json.put("Action", "delDepartment");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 发送消息
     * @param connectionId
     * @param listener
     */
    public void newMsgToServer(String connectionId, FromToMessage fromToMessage, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("SessionId", fromToMessage.sessionId);
            json.put("MsgType", fromToMessage.msgType);
            json.put("Platform", "android");
            json.put("Type", fromToMessage.type);
            json.put("Message", fromToMessage.message);
            json.put("VoiceSecond", fromToMessage.voiceSecond);
            json.put("DeviceInfo", "android device");
            json.put("Action", "newMsg");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 取消息
     * @param connectionId
     * @param listener
     */
    public void getMsg(String connectionId, ArrayList array, final ResponseListener listener) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ConnectionId", Utils.replaceBlank(connectionId));
        map.put("ReceivedMsgIds", array);
        map.put("Action", "getMsg");
        JSONWriter jw = new JSONWriter();
        jw.write(map);
        String content = jw.write(map);
        sendPost(content, listener);
    }


    /**
     * 取大量消息
     * @param connectionId
     * @param listener
     */
    public void getLargeMsgs(String connectionId, ArrayList largeMsgIdarray, final ResponseListener listener) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ConnectionId", Utils.replaceBlank(connectionId));
        map.put("LargeMsgId", largeMsgIdarray);
        map.put("Action", "getLargeMsg");
        JSONWriter jw = new JSONWriter();
        jw.write(map);
        String content = jw.write(map);
        sendPost(content, listener);
    }

    /**
     * 获取联系人
     * @param connectionId
     * @return
     */
    public Observable<List<Contacts>> getContacts(final String connectionId) {
        return Observable.create(new Observable.OnSubscribe<List<Contacts>>() {
            @Override
            public void call(final Subscriber<? super List<Contacts>> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "getContacts");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    List<Contacts> contactsList = HttpParser.getContacts(st);
                                    subscriber.onNext(contactsList);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 获取群组
     * @param connectionId
     * @return
     */
    public Observable<List<Group>> getGroupByUser(final String connectionId) {
        return Observable.create(new Observable.OnSubscribe<List<Group>>() {
            @Override
            public void call(final Subscriber<? super List<Group>> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "getGroupByUser");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    LogUtil.d("从网络加载群组返回数据:"+st);
                                    MobileApplication.cacheUtil.put(CacheKey.CACHE_GROUP, st);

                                    List<Group> groupsList = HttpParser.getGroups(st);
                                    subscriber.onNext(groupsList);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    /**
     * 创建群组
     * @param connectionId
     * @return
     */
    public Observable<String> createGroup(final String connectionId,final ArrayList admins,
                                          final ArrayList members, final String name) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("ConnectionId", Utils.replaceBlank(connectionId));
                    map.put("Member", members);
                    map.put("Admin", admins);
                    map.put("Title", name);
                    map.put("Action", "createGroup");
                    JSONWriter jw = new JSONWriter();
                    String content = jw.write(map);
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    LogUtil.d("网络创建群组返回数据:"+st);
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    /**
     * 修改群组名字
     * @param connectionId
     * @return
     */
    public Observable<String> updateGroupTitle(final String connectionId,final String id,
                                          final String title) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "editGroup");
                    json.put("Title", title);
                    json.put("_id", id);
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 添加群管理员
     * @param connectionId
     * @return
     */
    public Observable<String> addGroupAdmin(final String connectionId,final String _id, final ArrayList admin) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("ConnectionId", Utils.replaceBlank(connectionId));
                    map.put("_id", _id);
                    map.put("Admin", admin);
                    map.put("Action", "addGroupAdmin");
                    JSONWriter jw = new JSONWriter();
                    String content = jw.write(map);
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    /**
     * 添加群成员
     * @param connectionId
     * @return
     */
    public Observable<String> addGroupMember(final String connectionId,final String _id, final ArrayList member) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("ConnectionId", Utils.replaceBlank(connectionId));
                    map.put("_id", _id);
                    map.put("Member", member);
                    map.put("Action", "addGroupMember");
                    JSONWriter jw = new JSONWriter();
                    String content = jw.write(map);
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 删除群组
     * @param connectionId
     * @return
     */
    public Observable<String> deleteGroup(final String connectionId,final String id) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "removeGroup");
                    json.put("_id", id);
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    /**
     * 获取讨论组
     * @param connectionId
     * @return
     */
    public Observable<List<Discussion>> getDiscussionByUser(final String connectionId) {
        return Observable.create(new Observable.OnSubscribe<List<Discussion>>() {
            @Override
            public void call(final Subscriber<? super List<Discussion>> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "getDiscussionByUser");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    LogUtil.d("从网络加载讨论组返回数据:"+st);
                                    MobileApplication.cacheUtil.put(CacheKey.CACHE_DISCUSSION, st);

                                    List<Discussion> discussionList = HttpParser.getDiscussion(st);
                                    subscriber.onNext(discussionList);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }
    /**
     * 创建讨论组
     * @param connectionId
     * @return
     */
    public Observable<String> createDiscussion(final String connectionId,
                                          final ArrayList members, final String name) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("ConnectionId", Utils.replaceBlank(connectionId));
                    map.put("Member", members);
                    map.put("Title", name);
                    map.put("Action", "createDiscussion");
                    JSONWriter jw = new JSONWriter();
                    String content = jw.write(map);
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    LogUtil.d("网络创建讨论组返回数据:"+st);
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 删除讨论组
     * @param connectionId
     * @return
     */
    public Observable<String> deleteDiscussion(final String connectionId,final String id) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "removeDiscussion");
                    json.put("_id", id);
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 修改讨论组名字
     * @param connectionId
     * @return
     */
    public Observable<String> updateDiscussionTitle(final String connectionId,final String id,
                                               final String title) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("ConnectionId", Utils.replaceBlank(connectionId));
                    json.put("Action", "editDiscussion");
                    json.put("Title", title);
                    json.put("_id", id);
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 添加讨论组成员
     * @param connectionId
     * @return
     */
    public Observable<String> addDiscussionMember(final String connectionId,final String _id, final ArrayList member) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("ConnectionId", Utils.replaceBlank(connectionId));
                    map.put("_id", _id);
                    map.put("Member", member);
                    map.put("Action", "addDiscussionMember");
                    JSONWriter jw = new JSONWriter();
                    String content = jw.write(map);
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttp1)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext("success");
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 修改用户信息
     * @param connectionId
     * @param listener
     */
    public void editUserInfo(String connectionId,String _id, String name,
                             String email, String mobile, String product, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "editUserInfo");
            json.put("_id", _id);
            json.put("DisplayName", name);
            json.put("Email", email);
            json.put("Mobile", mobile);
            json.put("Product", product);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }

    /**
     * 修改用户头像
     * @param connectionId
     * @param listener
     */
    public void updateUserIcon(String connectionId,String iconUrl, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "uploadIcon");
            json.put("IconUrl", iconUrl);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPost(content, listener);
    }


    /**
     * 下载文件
     */
    public void downloadFile(String url, final File file,
                             final FileDownLoadListener listener) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(listener != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });

                }
            }
            @Override
            public void onResponse(Response response){
                if(listener != null && file != null) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;
                    try {
                        is = response.body().byteStream();
                        final long total = response.body().contentLength();
                        long sum = 0;

                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1)
                        {
                            sum += len;
                            fos.write(buf, 0, len);
                            final long finalSum = sum;
                            mDelivery.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onProgress((int)(finalSum * 100.0f / total));
                                }
                            });
                        }
                        fos.flush();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(file);
                            }
                        });
                    }catch (IOException e) {
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailed();
                            }
                        });
                    }finally {
                        try {
                            if (is != null) is.close();
                        } catch (IOException e) {
                        }
                        try {
                            if (fos != null) fos.close();
                        } catch (IOException e){
                        }
                    }
                }
            }
        });
    }

    //=========手机助手相关====================

    /**
     * 获取坐席缓存
     * @return
     */
    public Observable<String> getAgentCache(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "agents");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            LogUtil.d("获取坐席缓存数据:"+st);
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 获取技能组缓存
     * @return
     */
    public Observable<String> getQueueCache(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "queues");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 获取option缓存
     * @return
     */
    public Observable<String> getOptionCache(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "options");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 获取通话记录
     * @param listener
     */
    public void queryCdr(String sessionId, HashMap<String, String> datas, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.queryCdr");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 获取工单流程缓存
     * @return
     */
    public Observable<String> getBusinessFlow(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "businessFlow");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    /**
     * 获取工单步骤缓存
     * @return
     */
    public Observable<String> getBusinessStep(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "businessFlowStep");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 获取工单字段缓存
     * @return
     */
    public Observable<String> getBusinessField(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "businessFlowField");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    /**
     * 获取待领取工单
     * @param listener
     */
    public void queryRoleUnDealOrder(String sessionId, HashMap<String, String> datas, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getRoleUnDealBusiness");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 获取待处理工单
     * @param listener
     */
    public void queryUserUnDealOrder(String sessionId, HashMap<String, String> datas, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getUnDealBusiness");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 获取参与的工单
     * @param listener
     */
    public void queryFollowedOrder(String sessionId, HashMap<String, String> datas, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getFollowedBusiness");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 获取创建工单
     * @param listener
     */
    public void queryAssignedOrder(String sessionId, HashMap<String, String> datas, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getAssignedBusiness");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }


    /**
     * 领取工单
     * @param listener
     */
    public void haveThisOrder(String sessionId, HashMap<String, String> datas, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "setTaskToMe");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 获取工单详情
     * @param listener
     */
    public void getBusinessDetailById(String sessionId, String busId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "getBusinessDetailById");
            json.put("_id", busId);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 保存备注
     * @param listener
     */
    public void saveBusinessBackInfo(String sessionId, String busId, String backInfo, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "addBusinessBackInfo");
            json.put("_id", busId);
            json.put("backInfo", backInfo);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 执行动作操作
     * @param listener
     */
    public void excuteBusinessStepAction(String sessionId, HashMap<String, String> datas, HashMap<String, JSONArray> jadata, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "excuteBusinessStepAction");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
            for (String key : jadata.keySet()) {
                json.put(key, jadata.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 重新提交工单
     * @param listener
     */
    public void reSaveBusiness(String sessionId, HashMap<String, String> datas, HashMap<String, JSONArray> jadata, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "addBusinessTask");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
            for (String key : jadata.keySet()) {
                json.put(key, jadata.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     *
     * @param listener
     */
    public void getErpQiNiuToken(final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("Action", "app.weixin.getUptoken");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        RequestBody formBody = new FormEncodingBuilder()
                .add("data", content)
                .build();
        Request request = new Request.Builder()
                .url(RequestUrl.baseHttpMobileQiNiu)
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(listener != null) {
                    mDelivery.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed();
                        }
                    });

                }
            }
            @Override
            public void onResponse(Response response){
                if(listener != null) {
                    final String responseStr;
                    try {
                        responseStr = response.body().string();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(responseStr);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 重新提交工单
     * @param listener
     */
    public void excuteBusinessBackAction(String sessionId, String busId, String backInfo,ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "excuteBusinessBackAction");
            json.put("_id", busId);
            json.put("backInfo", backInfo);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    /**
     *
     * 设置工单推送是否开启
     * @param sessionId
     * @param listener
     */
    public void setErpPush(String sessionId, boolean on, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.alterBMRefuseStatus");
            json.put("refuse", on);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    public void setErpPushMy(String sessionId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.alterBMRefuseStatus");
            json.put("refuse", "MINE");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    public Observable<String> getCdrCache(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "getCdrCache");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            LogUtil.d("获取cdr缓存数据:"+st);
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<String> getErpCache(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    LogUtil.d("获取erp缓存数据开始");
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "getErpCache");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            LogUtil.d("获取erp缓存数据:"+st);
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 获取工作台数据
     * @param sessionId
     * @return
     */
    public Observable<String> getWorkBenchInfo(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "mobileAssistant.getWorkBenchInfo");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 报表
     * @param sessionId
     * @return
     */
    public Observable<String> doReport(final String sessionId, final String type, final String time) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "mobileAssistant.doReport");
                    json.put("report_type", type);
                    json.put("time_type", time);
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    public Observable<String> refreshAgentWorkReport(final String sessionId, final String time, final List<String> agentIds) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("sessionId", Utils.replaceBlank(sessionId));
                    map.put("agents", agentIds);
                    map.put("action", "mobileAssistant.doReport");
                    map.put("report_type", "agentwork");
                    map.put("time_type", time);
                    JSONWriter jw = new JSONWriter();

                    String content = jw.write(map);
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSucceed(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<String> getCustomerCache(final String sessionId) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    LogUtil.d("获取customer缓存数据开始");
                    JSONObject json = new JSONObject();
                    json.put("sessionId", Utils.replaceBlank(sessionId));
                    json.put("action", "common.getDicCache");
                    json.put("type", "custTmpls");
                    String content = json.toString();
                    RequestBody formBody = new FormEncodingBuilder()
                            .add("data", content)
                            .build();
                    Request request = new Request.Builder()
                            .url(RequestUrl.baseHttpMobile)
                            .post(formBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            String st = response.body().string();
                            LogUtil.d("获取customer缓存数据:"+st);
                            if(!subscriber.isUnsubscribed()) {
                                if(HttpParser.getSuccess(st)) {
                                    subscriber.onNext(st);
                                    subscriber.onCompleted();
                                }else {
                                    throw new IOException("get data failed");
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public void queryCustomerList(String sessionId, HashMap<String, Object> map, final ResponseListener listener) {

        map.put("sessionId", Utils.replaceBlank(sessionId));
        map.put("action", "mobileAssistant.doCustomer");
        map.put("real_action", "customer.queryCustPage2In");
        JSONWriter jw = new JSONWriter();

        String content = jw.write(map);
        sendPostForMobileAssistant(content, listener);
    }

    public void queryCustomerInfo(String sessionId, String customerId, final ResponseListener listener) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", Utils.replaceBlank(sessionId));
        map.put("action", "mobileAssistant.doCustomer");
        map.put("real_action", "customer.queryCustInfo");
        map.put("_id", customerId);
        JSONWriter jw = new JSONWriter();

        String content = jw.write(map);
        sendPostForMobileAssistant(content, listener);
    }

    public void customer_addNote(String sessionId, HashMap<String, Object> map, final ResponseListener listener) {

        map.put("sessionId", Utils.replaceBlank(sessionId));
        map.put("action", "mobileAssistant.doCustomer");
        map.put("real_action", "customer.addNote");
        JSONWriter jw = new JSONWriter();

        String content = jw.write(map);
        sendPostForMobileAssistant(content, listener);
    }

    public void customer_dealNote(String sessionId, HashMap<String, Object> map, final ResponseListener listener) {

        map.put("sessionId", Utils.replaceBlank(sessionId));
        map.put("action", "mobileAssistant.doCustomer");
        map.put("real_action", "customer.dealNote");
        JSONWriter jw = new JSONWriter();

        String content = jw.write(map);
        sendPostForMobileAssistant(content, listener);
    }

    /**
     * 新建工单
     * @param listener
     */
    public void addBusiness(String sessionId, HashMap<String, String> datas, HashMap<String, JSONArray> jadata, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doBusiness");
            json.put("real_action", "addBusinessTask");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
            for (String key : jadata.keySet()) {
                json.put(key, jadata.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }

    public void customer_queryCommonHistory(String sessionId, String customerId, final ResponseListener listener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", Utils.replaceBlank(sessionId));
        map.put("action", "mobileAssistant.doCustomer");
        map.put("real_action", "customer.queryCustCommonHistory");
        map.put("customer", customerId);
        JSONWriter jw = new JSONWriter();

        String content = jw.write(map);
        sendPostForMobileAssistant(content, listener);
    }

    public void updateCustomerStatusOrSource(String sessionId, HashMap<String, Object> map, final ResponseListener listener) {

        map.put("sessionId", Utils.replaceBlank(sessionId));
        map.put("action", "mobileAssistant.doCustomer");
        map.put("real_action", "customer.updateCustomerStatusOrSource");
        JSONWriter jw = new JSONWriter();

        String content = jw.write(map);
        sendPostForMobileAssistant(content, listener);
    }

    public void customer_update(String sessionId, HashMap<String, String> datas, HashMap<String, JSONArray> jadata, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId", Utils.replaceBlank(sessionId));
            json.put("action", "mobileAssistant.doCustomer");
            json.put("real_action", "customer.updateCustomer");
            for(String key : datas.keySet()) {
                json.put(key, datas.get(key));
            }
            for (String key : jadata.keySet()) {
                json.put(key, jadata.get(key));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
        sendPostForMobileAssistant(content, listener);
    }
}
