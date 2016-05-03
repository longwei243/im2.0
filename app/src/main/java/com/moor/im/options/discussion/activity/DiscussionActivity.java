package com.moor.im.options.discussion.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.event.DiscussionCreateSuccess;
import com.moor.im.common.event.GroupCreateSuccess;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.Group;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.discussion.adapter.DiscussionAdapter;
import com.moor.im.options.group.activity.CreateGroupActivity;
import com.moor.im.options.group.adapter.GroupAdapter;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/4/25.
 */
public class DiscussionActivity extends BaseActivity{

    private RecyclerView mRecycleView;
    private DiscussionAdapter mAdapter;

    User user = UserDao.getInstance().getUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        setupTitleBar();

        mRecycleView = (RecyclerView) findViewById(R.id.discussion_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(linearLayoutManager);
        mAdapter = new DiscussionAdapter();
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new EasyRecyclerViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View convertView, int position) {
                Discussion discussion = (Discussion) mAdapter.getList().get(position);
                Intent chatIntent = new Intent(DiscussionActivity.this, ChatActivity.class);
                chatIntent.putExtra("type", "Discussion");
                chatIntent.putExtra("_id", NullUtil.checkNull(discussion._id));
                chatIntent.putExtra("otherName", NullUtil.checkNull(discussion.title));
                startActivity(chatIntent);
                finish();
            }
        });
        addRxBuxListener();
        getData();
    }

    private void setupTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("讨论组");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton titlebar_add = (ImageButton) findViewById(R.id.titlebar_add);
        titlebar_add.setVisibility(View.VISIBLE);
        titlebar_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createDiscussionIntent = new Intent(DiscussionActivity.this, CreateDiscussionActivity.class);
                startActivity(createDiscussionIntent);
            }
        });

    }

    private void getData() {
        showLoadingDialog();
        Observable<List<Discussion>> netData = getDataFromNetWithSave();

        mCompositeSubscription.add(netData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Discussion>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.d("获取讨论组数据失败了");
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onNext(List<Discussion> discussions) {
                        dismissLoadingDialog();
                        mAdapter.setList(discussions);
                        mAdapter.notifyDataSetChanged();
                    }
                }));
    }



    /**
     * 从网络加载数据并保存到本地文件
     * @return
     */
    private Observable<List<Discussion>> getDataFromNetWithSave() {
        LogUtil.d("从网络中获取讨论组数据");
        return HttpManager.getInstance().getDiscussionByUser(InfoDao.getInstance().getConnectionId());
    }

    private void addRxBuxListener() {
        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof DiscussionCreateSuccess) {
                            getData();
                        }
                    }
                }));
    }

}
