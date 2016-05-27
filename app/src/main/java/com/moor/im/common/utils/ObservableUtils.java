package com.moor.im.common.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.MobileAssitantParser;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MABusinessField;
import com.moor.im.options.mobileassistant.model.MABusinessFlow;
import com.moor.im.options.mobileassistant.model.MABusinessStep;
import com.moor.im.options.mobileassistant.model.MAOption;
import com.moor.im.options.mobileassistant.model.MAQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/4/26.
 */
public class ObservableUtils {

    /**
     * 从数据库获取联系人
     * @return
     */
    public static Observable<List<Contacts>> getContactsFormDB() {

        return Observable.create(new Observable.OnSubscribe<List<Contacts>>() {
            @Override
            public void call(Subscriber<? super List<Contacts>> subscriber) {
                List<Contacts> contactsList = ContactsDao.getInstance().getContacts();
                if(!subscriber.isUnsubscribed()) {
                    if(contactsList != null) {
                        LogUtil.d("从数据库加载联系人");
                        subscriber.onNext(contactsList);
                        subscriber.onCompleted();
                    }else {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }
                }

            }
        });
    }

    /**
     * 从网络获取坐席缓存
     * @param sessionId
     * @return
     */
    public static Observable<String> getAgentCacheObservable(String sessionId) {
        return HttpManager.getInstance().getAgentCache(sessionId)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LogUtil.d("获取坐席缓存数据:"+s);
                        try {
                            JSONObject o = new JSONObject(s);
                            if(o.getBoolean("success")) {
                                List<MAAgent> agents = MobileAssitantParser.getAgents(s);
                                HashMap<String, MAAgent> agentDatas = MobileAssitantParser.transformAgentData(agents);
                                MobileAssitantCache.getInstance().setAgentMap(agentDatas);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }
    /**
     * 从网络获取技能组缓存
     * @param sessionId
     * @return
     */
    public static Observable<String> getQueueCacheObservable(String sessionId) {
        return HttpManager.getInstance().getQueueCache(sessionId)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
//                        LogUtil.d("获取技能组缓存数据:"+s);
                        try {
                            JSONObject o = new JSONObject(s);
                            if(o.getBoolean("success")) {
                                List<MAQueue> queues = MobileAssitantParser.getQueues(s);
                                HashMap<String, MAQueue> queueDatas = MobileAssitantParser.transformQueueData(queues);
                                MobileAssitantCache.getInstance().setQueueMap(queueDatas);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }
    /**
     * 从网络获取配置信息缓存
     * @param sessionId
     * @return
     */
    public static Observable<String> getOptionCacheObservable(String sessionId) {
        return HttpManager.getInstance().getOptionCache(sessionId)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
//                        LogUtil.d("获取option缓存数据:"+s);
                        try {
                            JSONObject o = new JSONObject(s);
                            if(o.getBoolean("success")) {
                                List<MAOption> options = MobileAssitantParser.getOptions(s);
                                HashMap<String, MAOption> optionDatas = MobileAssitantParser.transformOptionData(options);
                                MobileAssitantCache.getInstance().setOptionMap(optionDatas);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 从网络获取工单流程缓存
     * @param sessionId
     * @return
     */
    public static Observable<String> getBusinessFlowCacheObservable(String sessionId) {
        return HttpManager.getInstance().getBusinessFlow(sessionId)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LogUtil.d("获取getBusinessFlow缓存数据:"+s);
                        try {
                            JSONObject o = new JSONObject(s);
                            if(o.getBoolean("success")) {
                                List<MABusinessFlow> businessFlows = MobileAssitantParser.getBusinessFlow(s);
                                HashMap<String, MABusinessFlow> businessFlowsMap = MobileAssitantParser.transformBusinessFlowData(businessFlows);
                                MobileAssitantCache.getInstance().setFlowMap(businessFlowsMap);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }
    /**
     * 从网络获取工单步骤缓存
     * @param sessionId
     * @return
     */
    public static Observable<String> getBusinessStepCacheObservable(String sessionId) {
        return HttpManager.getInstance().getBusinessStep(sessionId)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
//                        LogUtil.d("获取getBusinessStep缓存数据:"+s);
                        try {
                            JSONObject o = new JSONObject(s);
                            if(o.getBoolean("success")) {
                                List<MABusinessStep> businessSteps = MobileAssitantParser.getBusinessStep(s);
                                HashMap<String, MABusinessStep> businessStepMap = MobileAssitantParser.transformBusinessStepData(businessSteps);
                                MobileAssitantCache.getInstance().setStepMap(businessStepMap);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 从网络获取工单字段缓存
     * @param sessionId
     * @return
     */
    public static Observable<String> getBusinessFieldCacheObservable(String sessionId) {
        return HttpManager.getInstance().getBusinessField(sessionId)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
//                        LogUtil.d("获取getBusinessField缓存数据:"+s);
                        try {
                            JSONObject o = new JSONObject(s);
                            if(o.getBoolean("success")) {
                                List<MABusinessField> businessFields = MobileAssitantParser.getBusinessField(s);
                                HashMap<String, MABusinessField> businessFieldData = MobileAssitantParser.transformBusinessFieldData(businessFields);
                                MobileAssitantCache.getInstance().setFieldMap(businessFieldData);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public static Observable<String> getCdrCache(String sessionId) {
        return HttpManager.getInstance().getCdrCache(sessionId).doOnNext(new Action1<String>() {
            @Override
            public void call(String s) {
                List<MAAgent> agents = new ArrayList<>();
                List<MAQueue> queues = new ArrayList<>();
                try {
                    Gson gson = new Gson();
                    JSONObject o = new JSONObject(s);
                    if(o.getBoolean("success")) {
                        JSONObject jb = o.getJSONObject("data");
                        JSONArray agentsArray = jb.getJSONArray("agents");
                        agents = gson.fromJson(agentsArray.toString(),
                                new TypeToken<List<MAAgent>>() {
                                }.getType());
                        HashMap<String, MAAgent> agentDatas = MobileAssitantParser.transformAgentData(agents);
                        MobileAssitantCache.getInstance().setAgentMap(agentDatas);

                        JSONArray queuesArray = jb.getJSONArray("queues");
                        queues = gson.fromJson(queuesArray.toString(),
                                new TypeToken<List<MAQueue>>() {
                                }.getType());
                        HashMap<String, MAQueue> queuesDatas = MobileAssitantParser.transformQueueData(queues);
                        MobileAssitantCache.getInstance().setQueueMap(queuesDatas);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<String> getErpCache(String sessionId) {
        return HttpManager.getInstance().getErpCache(sessionId).doOnNext(new Action1<String>() {
            @Override
            public void call(String s) {
                List<MAAgent> agents = new ArrayList<>();
                List<MAQueue> queues = new ArrayList<>();
                List<MAOption> options = new ArrayList<>();
                List<MABusinessStep> businessSteps = new ArrayList<>();
                List<MABusinessField> businessFields = new ArrayList<>();
                List<MABusinessFlow> businessFlows = new ArrayList<>();
                LogUtil.d("处理工单缓存开始");
                try {
                    Gson gson = new Gson();
                    JSONObject o = new JSONObject(s);
                    if(o.getBoolean("success")) {
                        JSONObject jb = o.getJSONObject("data");
                        JSONArray agentsArray = jb.getJSONArray("agents");
                        agents = gson.fromJson(agentsArray.toString(),
                                new TypeToken<List<MAAgent>>() {
                                }.getType());
                        HashMap<String, MAAgent> agentDatas = MobileAssitantParser.transformAgentData(agents);
                        MobileAssitantCache.getInstance().setAgentMap(agentDatas);

                        JSONArray queuesArray = jb.getJSONArray("queues");
                        queues = gson.fromJson(queuesArray.toString(),
                                new TypeToken<List<MAQueue>>() {
                                }.getType());
                        HashMap<String, MAQueue> queuesDatas = MobileAssitantParser.transformQueueData(queues);
                        MobileAssitantCache.getInstance().setQueueMap(queuesDatas);

                        JSONArray optionsArray = jb.getJSONArray("options");
                        options = gson.fromJson(optionsArray.toString(),
                                new TypeToken<List<MAOption>>() {
                                }.getType());
                        HashMap<String, MAOption> optionDatas = MobileAssitantParser.transformOptionData(options);
                        MobileAssitantCache.getInstance().setOptionMap(optionDatas);

                        JSONArray stepsArray = jb.getJSONArray("steps");
                        businessSteps = gson.fromJson(stepsArray.toString(),
                                new TypeToken<List<MABusinessStep>>() {
                                }.getType());
                        HashMap<String, MABusinessStep> businessStepMap = MobileAssitantParser.transformBusinessStepData(businessSteps);
                        MobileAssitantCache.getInstance().setStepMap(businessStepMap);

                        JSONArray fieldsArray = jb.getJSONArray("fields");
                        businessFields = gson.fromJson(fieldsArray.toString(),
                                new TypeToken<List<MABusinessField>>() {
                                }.getType());
                        HashMap<String, MABusinessField> businessFieldData = MobileAssitantParser.transformBusinessFieldData(businessFields);
                        MobileAssitantCache.getInstance().setFieldMap(businessFieldData);

                        JSONArray flowsArray = jb.getJSONArray("flows");
                        businessFlows = gson.fromJson(flowsArray.toString(),
                                new TypeToken<List<MABusinessFlow>>() {
                                }.getType());
                        HashMap<String, MABusinessFlow> businessFlowsMap = MobileAssitantParser.transformBusinessFlowData(businessFlows);
                        MobileAssitantCache.getInstance().setFlowMap(businessFlowsMap);
                    }
                    LogUtil.d("处理工单缓存结束");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}

