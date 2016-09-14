package com.moor.im.options.mobileassistant.customer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by longwei on 16/9/6.
 */
public class CustCacheUtil {

    public static String getStatus(String custCacheStr, String dbtype, String statusStr) {
        String s = "";
        try {
            JSONObject jsonObject = new JSONObject(custCacheStr);
            if(dbtype.equals(jsonObject.getString("_id"))) {
                JSONObject status = jsonObject.getJSONObject("status");
                s = status.getString(statusStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return s;
    }
}
