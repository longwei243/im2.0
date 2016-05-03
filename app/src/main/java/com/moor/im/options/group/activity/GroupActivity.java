package com.moor.im.options.group.activity;

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
import com.moor.im.common.event.GroupCreateSuccess;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Group;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.activity.ChatActivity;
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
public class GroupActivity extends BaseActivity{

    private RecyclerView mRecycleView;
    private GroupAdapter mAdapter;

    User user = UserDao.getInstance().getUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        setupTitleBar();

        mRecycleView = (RecyclerView) findViewById(R.id.group_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(linearLayoutManager);
        mAdapter = new GroupAdapter();
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new EasyRecyclerViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View convertView, int position) {
                Group group = (Group) mAdapter.getList().get(position);
                Intent chatIntent = new Intent(GroupActivity.this, ChatActivity.class);
                chatIntent.putExtra("type", "Group");
                chatIntent.putExtra("_id", NullUtil.checkNull(group._id));
                chatIntent.putExtra("otherName", NullUtil.checkNull(group.title));
                startActivity(chatIntent);
                finish();
            }
        });
        addRxBuxListener();
        getData();
    }

    private void setupTitleBar() {
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("群组");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton titlebar_add = (ImageButton) findViewById(R.id.titlebar_add);

        String product = user.product;
        if("zj".equals(NullUtil.checkNull(product))) {
            boolean isAdmin = user.isAdmin;
            if(!isAdmin) {
                titlebar_add.setVisibility(View.GONE);
            }else {
                titlebar_add.setVisibility(View.VISIBLE);
            }
        }else if("cc".equals(NullUtil.checkNull(product))) {
            String type = user.type;
            if(!"manager".equals(NullUtil.checkNull(type))) {
                titlebar_add.setVisibility(View.GONE);
            }else {
                titlebar_add.setVisibility(View.VISIBLE);
            }
        }
        titlebar_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createGroupIntent = new Intent(GroupActivity.this, CreateGroupActivity.class);
                startActivity(createGroupIntent);
            }
        });

    }

    private void getData() {
        showLoadingDialog();
        Observable<List<Group>> netData = getDataFromNetWithSave();

        mCompositeSubscription.add(netData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Group>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.d("获取群组数据失败了");
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onNext(List<Group> groups) {
                        dismissLoadingDialog();
                        mAdapter.setList(groups);
                        mAdapter.notifyDataSetChanged();
                    }
                }));
    }



    /**
     * 从网络加载数据并保存到本地文件
     * @return
     */
    private Observable<List<Group>> getDataFromNetWithSave() {
        LogUtil.d("从网络中获取群组数据");
        return HttpManager.getInstance().getGroupByUser(InfoDao.getInstance().getConnectionId());
    }

    /**
     * 从本地缓存中加载,没用
     * @return
     */
    private Observable<List<Group>> getDataFromLoaclCache() {

        return Observable.create(new Observable.OnSubscribe<List<Group>>() {
            @Override
            public void call(Subscriber<? super List<Group>> subscriber) {
                String groupStr = MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_GROUP);
                if(groupStr != null) {
                    LogUtil.d("从本地缓存中获取群组数据");
                    List<Group> groups = HttpParser.getGroups(groupStr);
                    subscriber.onNext(groups);
                    subscriber.onCompleted();
                }else {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            }
        });
    }


    private void addRxBuxListener() {
        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof GroupCreateSuccess) {
                            getData();
                        }
                    }
                }));
    }

}
