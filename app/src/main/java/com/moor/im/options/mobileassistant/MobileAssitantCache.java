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

    private HashMap<String, MAAgent> agentMap = new HashMap<>();
    private HashMap<String, MAQueue> queueMap = new HashMap<>();
    private HashMap<String, MAOption> optionMap = new HashMap<>();
    private HashMap<String, MABusinessStep> stepMap = new HashMap<>();
    private HashMap<String, MABusinessFlow> flowMap = new HashMap<>();
    private HashMap<String, MABusinessField> fieldMap = new HashMap<>();


    private MobileAssitantCache() {
    }

    public static MobileAssitantCache getInstance() {
        if(instance == null) {
            instance = new MobileAssitantCache();
        }
        return instance;
    }

    public MAAgent getAgentById(String id) {
        MAAgent agent = agentMap.get(id);
        if (agent != null) {
            return agent;
        }
        return null;
    }

    public List<MAAgent> getAgents() {
        List<MAAgent> agents = new ArrayList<>();
        for (String key : agentMap.keySet()) {
            MAAgent agent = agentMap.get(key);
            agents.add(agent);
        }
        return agents;

    }

    public MAQueue getQueueByExten(String id) {
        MAQueue queue = queueMap.get(id);
        if(queue != null) {
            return queue;
        }
        return null;
    }

    public MABusinessStep getBusinessStep(String id) {
        MABusinessStep step = stepMap.get(id);
        if(step != null) {
            return step;
        }
        return null;
    }

    public MAAction getBusinessStepAction(String stepId, String actionId) {
        MABusinessStep step = stepMap.get(stepId);
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
        return null;
    }

    public MABusinessFlow getBusinessFlow(String id) {
        MABusinessFlow flow = flowMap.get(id);
        if(flow != null) {
            return flow;
        }
        return null;
    }

    public List<MABusinessFlow> getBusinessFlows() {
        List<MABusinessFlow> flows = new ArrayList<>();
        for (String key : flowMap.keySet()) {
            MABusinessFlow flow = flowMap.get(key);
            flows.add(flow);
        }
        return flows;
    }


    public MABusinessField getBusinessField(String id) {
        MABusinessField field = fieldMap.get(id);
        if(field != null) {
            return field;
        }

        return null;
    }

    public MAOption getMAOption(String dicId) {
        for(String key : optionMap.keySet()) {
            if(optionMap.get(key)._id.equals(dicId)) {
                return optionMap.get(key);
            }
        }
        return null;
    }

    public String getDicById(String id) {
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

    public void setAgentMap(HashMap<String, MAAgent> agentMap) {
        this.agentMap = agentMap;
    }

    public void setQueueMap(HashMap<String, MAQueue> queueMap) {
        this.queueMap = queueMap;
    }

    public void setOptionMap(HashMap<String, MAOption> optionMap) {
        this.optionMap = optionMap;
    }

    public void setStepMap(HashMap<String, MABusinessStep> stepMap) {
        this.stepMap = stepMap;
    }

    public void setFlowMap(HashMap<String, MABusinessFlow> flowMap) {
        this.flowMap = flowMap;
    }

    public void setFieldMap(HashMap<String, MABusinessField> fieldMap) {
        this.fieldMap = fieldMap;
    }
    public HashMap<String, MAAgent> getAgentMap() {
        return agentMap;
    }

    public HashMap<String, MAQueue> getQueueMap() {
        return queueMap;
    }

    public HashMap<String, MAOption> getOptionMap() {
        return optionMap;
    }

    public HashMap<String, MABusinessStep> getStepMap() {
        return stepMap;
    }

    public HashMap<String, MABusinessFlow> getFlowMap() {
        return flowMap;
    }

    public HashMap<String, MABusinessField> getFieldMap() {
        return fieldMap;
    }

    public void clear() {
        agentMap.clear();
        queueMap.clear();
        optionMap.clear();
        stepMap.clear();
        flowMap.clear();
        fieldMap.clear();
    }
}
