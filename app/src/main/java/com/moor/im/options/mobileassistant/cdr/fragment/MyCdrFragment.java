package com.moor.im.options.mobileassistant.cdr.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.CacheUtils;
import com.moor.im.common.utils.ObservableUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.pulltorefresh.PullToRefreshBase;
import com.moor.im.common.views.pulltorefresh.PullToRefreshListView;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.MobileAssitantParser;
import com.moor.im.options.mobileassistant.cdr.activity.MACallDetailActivity;
import com.moor.im.options.mobileassistant.cdr.activity.MYCallHighQueryActivity;
import com.moor.im.options.mobileassistant.cdr.adapter.MyCallAdapter;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MACallLogData;
import com.moor.im.options.mobileassistant.model.MAOption;
import com.moor.im.options.mobileassistant.model.MAQueue;
import com.moor.im.options.mobileassistant.model.Option;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/5/3.
 */
public class MyCdrFragment extends BaseLazyFragment{
    private static final String MYCALLQUERYTYPE = "myCallQueryType";

    private List<MACallLogData> maCallLogs;
    private PullToRefreshListView mPullRefreshListView;
    private MyCallAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingDialog loadingFragmentDialog;

    private int page = 2;

    private View view;
    private TextView mycall_tv_hignquery;
    private EditText mycall_et_numquery;
    private ImageButton mycall_ib_search;
    private Spinner mycall_sp_quickquery;

    private View footerView;

    private SharedPreferences myCallSp;
    private SharedPreferences.Editor myCallEditor;

    private TextView mycall_tv_queryitem;
    private ImageView mycall_btn_queryitem;
    private RelativeLayout mycall_rl_queryitem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mycdr, null);
        myCallSp = getActivity().getSharedPreferences(getResources().getString(R.string.mobileAssistant), 0);
        myCallEditor = myCallSp.edit();
        myCallEditor.clear();
        myCallEditor.commit();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);
        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.my_ptl);

        mycall_tv_hignquery = (TextView) view.findViewById(R.id.mycall_tv_hignquery);
        mycall_tv_hignquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MYCallHighQueryActivity.class);
                startActivityForResult(intent, 0x999);
            }
        });


        mycall_et_numquery = (EditText) view.findViewById(R.id.mycall_et_numquery);
        mycall_ib_search = (ImageButton) view.findViewById(R.id.mycall_ib_search);
        mycall_ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = mycall_et_numquery.getText().toString().trim();
                if (!"".equals(num)) {
                    HashMap<String, String> datas = new HashMap<String, String>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("NUMBER", num);
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "number");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), "请输入号码后查询", Toast.LENGTH_SHORT).show();
                }
            }
        });
        loadingFragmentDialog = new LoadingDialog();

        mycall_sp_quickquery = (Spinner) view.findViewById(R.id.mycall_sp_quickquery);
        final String[] quickDatas = getResources().getStringArray(R.array.mycall);
        ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, R.id.sp_tv, quickDatas) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout,
                        null);
                TextView label = (TextView) view
                        .findViewById(R.id.spinner_item_label);
                label.setText(quickDatas[position]);
                if (mycall_sp_quickquery.getSelectedItemPosition() == position) {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.grey_erp));
                } else {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.all_white));
                }
                return view;
            }
        };
        mycall_sp_quickquery.setAdapter(spAdapter);
        mycall_sp_quickquery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    myCallEditor.clear();
                    myCallEditor.commit();
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 2) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "dealing");
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 3) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "notDeal");
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 4) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "queueLeak");
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 5) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "voicemail");
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 6) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "leak");
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                } else if (position == 7) {
                    HashMap<String, String> datas = new HashMap<>();
                    datas.put("DISPOSAL_AGENT", user._id);
                    datas.put("STATUS", "blackList");
                    HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                    myCallEditor.putString(MYCALLQUERYTYPE, "quick");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    mycall_rl_queryitem.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mycall_rl_queryitem = (RelativeLayout) view.findViewById(R.id.mycall_rl_queryitem);
        mycall_tv_queryitem = (TextView) view.findViewById(R.id.mycall_tv_queryitem);
        mycall_btn_queryitem = (ImageView) view.findViewById(R.id.mycall_btn_queryitem);
        mycall_btn_queryitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mycall_rl_queryitem.setVisibility(View.GONE);
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                myCallEditor.clear();
                myCallEditor.commit();
                HashMap<String, String> datas = new HashMap<>();
                datas.put("DISPOSAL_AGENT", user._id);
                HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());

            }
        });

        initData2();
    }

    private void initData2() {
        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
        mCompositeSubscription.add(ObservableUtils.getCdrCache(user._id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingFragmentDialog.dismiss();
                        Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(String s) {
                        HashMap<String, String> datas = new HashMap<>();
                        datas.put("DISPOSAL_AGENT", user._id);
                        HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());

                    }
                }));
    }

    private void initData() {
        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
        mCompositeSubscription.add(ObservableUtils.getAgentCacheObservable(user._id)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtil.d("获取坐席缓存数据失败了");
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return ObservableUtils.getQueueCacheObservable(user._id)
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        LogUtil.d("获取技能组缓存数据失败了");
                                    }
                                });
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return ObservableUtils.getOptionCacheObservable(user._id);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingFragmentDialog.dismiss();
                        Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(String s) {
                        HashMap<String, String> datas = new HashMap<>();
                        datas.put("DISPOSAL_AGENT", user._id);
                        HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());

                    }
                }));

    }



    class QueryCdrResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            loadingFragmentDialog.dismiss();
            mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            Toast.makeText(getActivity(), "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {
                BackTask backTask = new BackTask();
                backTask.execute(responseString);
            } else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
            }
        }
    }

    class BackTask extends AsyncTask<String, Void, List<MACallLogData>> {

        @Override
        protected List<MACallLogData> doInBackground(String[] params) {
            maCallLogs = MobileAssitantParser.getCdrs(params[0]);
            return maCallLogs;
        }

        @Override
        protected void onPostExecute(List<MACallLogData> maCallLogDatas) {
            super.onPostExecute(maCallLogDatas);
            loadingFragmentDialog.dismiss();
            mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);

            mPullRefreshListView.getRefreshableView().removeFooterView(footerView);

            mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

                @Override
                public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                    loadDatasMore();
                }
            });
            mAdapter = new MyCallAdapter(getActivity(), maCallLogDatas);
            mPullRefreshListView.setAdapter(mAdapter);

            if(maCallLogDatas.size() < 10) {
                mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mPullRefreshListView.getRefreshableView().addFooterView(footerView);
            }

            page = 2;
            mPullRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MACallLogData maCallLogData = (MACallLogData) parent.getAdapter().getItem(position);
                    if (maCallLogData != null) {
                        Intent intent = new Intent(getActivity(), MACallDetailActivity.class);
                        intent.putExtra("calllogdata", maCallLogData);
                        startActivity(intent);
                    }
                }
            });

        }
    }


    /**
     * 加载更多数据
     */
    private void loadDatasMore() {

        String type = myCallSp.getString(MYCALLQUERYTYPE, "");
        if("".equals(type)) {
            HashMap<String, String> datas = new HashMap<>();
            datas.put("DISPOSAL_AGENT", user._id);
            datas.put("page", page + "");
            HttpManager.getInstance().queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("number".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MyCallQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("quick".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MyCallQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }else if("high".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MyCallQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryCdr(user._id, datas, new GetCdrMoreResponseHandler());
        }
    }
    class GetCdrMoreResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            mPullRefreshListView.onRefreshComplete();
            Toast.makeText(getActivity(), "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {

                BackTaskMore backTask = new BackTaskMore();
                backTask.execute(responseString);
            } else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
            }
        }
    }

    class BackTaskMore extends AsyncTask<String, Void, List<MACallLogData>> {

        @Override
        protected List<MACallLogData> doInBackground(String[] params) {
            List<MACallLogData> callLogs = MobileAssitantParser.getCdrs(params[0]);
            return callLogs;
        }

        @Override
        protected void onPostExecute(List<MACallLogData> maCallLogDatas) {
            super.onPostExecute(maCallLogDatas);
            if(maCallLogDatas.size() < 10) {
                //是最后一页了
                maCallLogs.addAll(maCallLogDatas);
                mAdapter.notifyDataSetChanged();
                mPullRefreshListView.onRefreshComplete();

                mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mPullRefreshListView.getRefreshableView().addFooterView(footerView);
            }else {
                maCallLogs.addAll(maCallLogDatas);
                mAdapter.notifyDataSetChanged();
                mPullRefreshListView.onRefreshComplete();
                page++;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x999 && resultCode == Activity.RESULT_OK) {
            if(data.getSerializableExtra("highQueryData") != null) {
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                mycall_sp_quickquery.setSelection(0);
                HashMap<String, String> datas = (HashMap<String, String>) data.getSerializableExtra("highQueryData");
                //显示查询的条件
                showQueryItem(datas);
                datas.put("DISPOSAL_AGENT", user._id);
                HttpManager.getInstance().queryCdr(user._id, datas, new QueryCdrResponseHandler());
                myCallEditor.putString(MYCALLQUERYTYPE, "high");
                myCallEditor.commit();
                MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCallQueryData, datas, CacheUtils.TIME_HOUR * 2);
            }
        }
    }

    private void showQueryItem(HashMap<String, String> datas) {
        StringBuilder sb = new StringBuilder();
        sb.append("查询条件:");
        for(String key : datas.keySet()) {
            sb.append(" ");
            if("CONNECT_TYPE".equals(key)) {
                String connectType = "";
                if("normal".equals(datas.get(key))) {
                    connectType = "普通来电";
                }else if("normal".equals(datas.get(key))) {
                    connectType = "外呼去电";
                }else if("transfer".equals(datas.get(key))) {
                    connectType = "来电转接";
                }else if("dialTransfer".equals(datas.get(key))) {
                    connectType = "外呼转接";
                }
                sb.append(connectType);
                continue;
            }
            if("STATUS".equals(key)) {
                String status = "";
                if("leak".equals(datas.get(key))) {
                    status = "IVR";
                }else if("dealing".equals(datas.get(key))) {
                    status = "已接听";
                }else if("notDeal".equals(datas.get(key))) {
                    status = "振铃未接听";
                }else if("queueLeak".equals(datas.get(key))) {
                    status = "排队放弃";
                }else if("voicemail".equals(datas.get(key))) {
                    status = "已留言";
                }else if("blackList".equals(datas.get(key))) {
                    status = "黑名单";
                }
                sb.append(status);
                continue;
            }

            if("DISPOSAL_AGENT".equals(key)) {
                MAAgent agent = MobileAssitantCache.getInstance().getAgentById(datas.get(key));
                String agentName = agent.displayName;
                sb.append(agentName);
                continue;
            }

            if("ERROR_MEMO".equals(key)) {
                MAQueue queue = MobileAssitantCache.getInstance().getQueueByExten(datas.get(key));
                String queueName = queue.DisplayName;
                sb.append(queueName);
                continue;
            }

            if("INVESTIGATE".equals(key)) {

//                if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
//                    HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
//                    for(String optionKey : optionMap.keySet()) {
//                        if("满意度调查选项".equals(optionKey)) {
//                            List<Option> investigates = optionMap.get(optionKey).options;
//                            for(int i=0; i<investigates.size(); i++) {
//                                if(datas.get(key).equals(investigates.get(i).options.get(0).name)) {
//                                    String investigateName = investigates.get(i).name;
//                                    sb.append(investigateName);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
                continue;
            }
            sb.append(datas.get(key));
        }
        mycall_rl_queryitem.setVisibility(View.VISIBLE);
        mycall_tv_queryitem.setText(sb.toString());
    }


}
