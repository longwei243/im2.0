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
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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

    /**
     * 获取用户信息
     * @param connectionId
     * @param listener
     */
    public void getUserInfo(String connectionId, final ResponseListener listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("ConnectionId", Utils.replaceBlank(connectionId));
            json.put("Action", "getUserInfo");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String content = json.toString();
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

    /**
     * 获取联系人版本号
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

    /**
     * 获取联系人版本号
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
                                    LogUtil.d("从网络加载联系人返回数据:"+st);
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


    /**
     * 下载文件
     */
    public void downloadFile(String url, final File file,
                             final FileDownLoadListener listener) {
        LogUtil.d("下载文件");
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
                                    listener.onProgress((int)(finalSum * 1.0f / total));
                                }
                            });
                        }
                        fos.flush();
                        mDelivery.post(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.d("文件下载成功，路径是:"+file.getAbsolutePath());
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
}
