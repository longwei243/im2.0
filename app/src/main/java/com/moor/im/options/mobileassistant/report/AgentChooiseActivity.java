package com.moor.im.options.mobileassistant.report;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.model.MAAgent;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by longwei on 16/8/16.
 */
public class AgentChooiseActivity extends Activity{

    private RecyclerView agent_chooise_rv_selected, agent_chooise_rv_agents;
    private TextView agent_chooise_tv_count;
    private AllAgentAdapter mAllAgentAdapter;
    private AgentSelectedAdapter mAgentSelectedAdapter;

    private CompositeSubscription mCompositeSubscription;

    private ArrayList<MAAgent> selectedAgents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_chooise);

        setTitleBar();

        mCompositeSubscription = new CompositeSubscription();

        agent_chooise_rv_selected = (RecyclerView) findViewById(R.id.agent_chooise_rv_selected);
        agent_chooise_rv_agents = (RecyclerView) findViewById(R.id.agent_chooise_rv_agents);
        agent_chooise_tv_count = (TextView) findViewById(R.id.agent_chooise_tv_count);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        agent_chooise_rv_selected.setLayoutManager(linearLayoutManager);
        mAgentSelectedAdapter = new AgentSelectedAdapter();
        agent_chooise_rv_selected.setAdapter(mAgentSelectedAdapter);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        agent_chooise_rv_agents.setLayoutManager(staggeredGridLayoutManager);
        mAllAgentAdapter = new AllAgentAdapter();
        agent_chooise_rv_agents.setAdapter(mAllAgentAdapter);
        //设置所有坐席数据
        List<MAAgent> allAgents = MobileAssitantCache.getInstance().getAgents();
        mAllAgentAdapter.setList(allAgents);

        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if (event instanceof AgentChecked) {
                            //更新列表数据
                            MAAgent agent = ((AgentChecked) event).mAgent;
                            selectedAgents.add(agent);
                            mAgentSelectedAdapter.setList(selectedAgents);
                            agent_chooise_rv_selected.scrollToPosition(selectedAgents.size() - 1);
                            mAgentSelectedAdapter.notifyDataSetChanged();
                            agent_chooise_tv_count.setText("已选"+selectedAgents.size()+"/4");
                        }else if (event instanceof AgentUnCheck) {
                            MAAgent agent = ((AgentUnCheck) event).mAgent;
                            selectedAgents.remove(agent);
                            mAgentSelectedAdapter.setList(selectedAgents);
                            agent_chooise_rv_selected.scrollToPosition(selectedAgents.size() - 1);
                            mAgentSelectedAdapter.notifyDataSetChanged();
                            agent_chooise_tv_count.setText("已选"+selectedAgents.size()+"/4");
                        }

                    }
                }));
    }

    private void setTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("选择坐席");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton titlebar_done = (ImageButton) findViewById(R.id.titlebar_done);
        titlebar_done.setVisibility(View.VISIBLE);
        titlebar_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedAgents.size() > 0 && selectedAgents.size() <= 4){
                    RxBus.getInstance().send(new AgentChooised(selectedAgents));
                    finish();
                }else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }
}
