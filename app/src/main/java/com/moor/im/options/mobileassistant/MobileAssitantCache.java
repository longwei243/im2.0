package com.moor.im.options.mobileassistant;

import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.model.User;
import com.moor.im.options.mobileassistant.model.MAAction;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MABusinessField;
import com.moor.im.options.mobileassistant.model.MABusinessFlow;
import com.moor.im.options.mobileassistant.model.MABusinessStep;
import com.moor.im.options.mobileassistant.model.MAOption;
import com.moor.im.options.mobileassistant.model.MAQueue;
import com.moor.im.options.mobileassistant.model.Option;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/18.
 */
public class MobileAssitantCache {

    private static  MobileAssitantCache instance;

    private User user;

    private MobileAssitantCache() {
        user = UserDao.getInstance().getUser();
    }

    public static MobileAssitantCache getInstance() {
        if(instance == null) {
            instance = new MobileAssitantCache();
        }
        return instance;
    }

    public MAAgent getAgentById(String id) {
        MAAgent agent;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent) != null) {
            HashMap<String, MAAgent> agentMap = (HashMap<String, MAAgent>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent);
            agent = agentMap.get(id);
            return agent;
        }else {
//            MobileHttpManager.getAgentCache(user._id, new GetAgentResponseHandler());
        }
        return null;
    }

    public List<MAAgent> getAgents() {
        List<MAAgent> agents = new ArrayList<>();
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent) != null) {
            HashMap<String, MAAgent> agentMap = (HashMap<String, MAAgent>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent);
            for (String key : agentMap.keySet()) {
                MAAgent agent = agentMap.get(key);
                agents.add(agent);
            }
            return agents;
        }
        return agents;
    }

    public MAQueue getQueueByExten(String id) {
        MAQueue queue;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue) != null) {
            HashMap<String, MAQueue> queueMap = (HashMap<String, MAQueue>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue);
            queue = queueMap.get(id);
            return queue;
        }else {

        }
        return null;
    }

    public MABusinessStep getBusinessStep(String id) {
        MABusinessStep step;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep) != null) {
            HashMap<String, MABusinessStep> stepMap = (HashMap<String, MABusinessStep>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep);
            step = stepMap.get(id);
            return step;
        }else {

        }
        return null;
    }

    public MAAction getBusinessStepAction(String stepId, String actionId) {
        MABusinessStep step;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep) != null) {
            HashMap<String, MABusinessStep> stepMap = (HashMap<String, MABusinessStep>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep);
            step = stepMap.get(stepId);
            if(step != null) {
                List<MAAction> actions = step.actions;
                if(actions != null) {
                    for(int i=0; i<actions.size(); i++) {
                        if(actions.get(i)._id.equals(actionId)) {
                            return actions.get(i);
                        }
                    }
                }
            }
        }else {

        }
        return null;
    }

    public MABusinessFlow getBusinessFlow(String id) {
        MABusinessFlow flow;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow) != null) {
            HashMap<String, MABusinessFlow> flowMap = (HashMap<String, MABusinessFlow>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow);
            flow = flowMap.get(id);
            return flow;
        }else {

        }
        return null;
    }

    public List<MABusinessFlow> getBusinessFlows() {
        List<MABusinessFlow> flows = new ArrayList<>();
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow) != null) {
            HashMap<String, MABusinessFlow> flowMap = (HashMap<String, MABusinessFlow>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow);
            for (String key : flowMap.keySet()) {
                MABusinessFlow flow = flowMap.get(key);
                flows.add(flow);
            }
            return flows;
        }else {

        }
        return null;
    }


    public MABusinessField getBusinessField(String id) {
        MABusinessField field;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessField) != null) {
            HashMap<String, MABusinessField> fieldMap = (HashMap<String, MABusinessField>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessField);
            field = fieldMap.get(id);
            return field;
        }else {

        }
        return null;
    }

    public MAOption getMAOption(String dicId) {
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
            HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
            for(String key : optionMap.keySet()) {
                if(optionMap.get(key)._id.equals(dicId)) {
                    return optionMap.get(key);
                }
            }
        }

        return null;
    }

    public String getDicById(String id) {
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
            HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
            for(String key : optionMap.keySet()) {
                if(optionMap.get(key)._id.equals(id)) {
                    return optionMap.get(key).name;
                }
                List<Option> options = optionMap.get(key).options;
                if(options != null && options.size() > 0) {
                    String result = getDic(id, options);
                    if(!"".equals(result)) {
                        return result;
                    }
                }

            }
        }else {

        }
        return "";
    }

    private String getDic(String key, List<Option> options) {

        for(int i=0; i<options.size(); i++) {
            Option opt = options.get(i);
            if(key.equals(opt.key)) {
                return opt.name;
            }
            List<Option> opts = opt.options;
            if(opts != null && opts.size() > 0) {
                String result = getDic(key, opts);
                if(result != null && !"".equals(result)) {
                    return result;
                }
            }

        }
        return "";
    }
}
