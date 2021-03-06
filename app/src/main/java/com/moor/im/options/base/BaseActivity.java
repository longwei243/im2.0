package com.moor.im.options.base;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.moor.im.app.MobileApplication;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.views.swipeback.SwipeBackActivity;
import com.moor.im.common.views.swipeback.SwipeBackLayout;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by longwei on 2016/3/15.
 */
public class BaseActivity extends SwipeBackActivity{

    /**
     * rxjava 订阅持有者
     */
    protected CompositeSubscription mCompositeSubscription
            = new CompositeSubscription();
    protected SwipeBackLayout mSwipeBackLayout;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        loadingDialog = new LoadingDialog();
        MobileApplication.getInstance().add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
        MobileApplication.getInstance().remove(this);
    }

    /**
     * 显示加载框
     */
    protected void showLoadingDialog() {
        loadingDialog.show(getSupportFragmentManager(), "loading");
    }

    /**
     * 隐藏加载框
     */
    protected void dismissLoadingDialog() {
        loadingDialog.dismissAllowingStateLoss();

    }
}
