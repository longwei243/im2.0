package com.moor.im.helptest.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.moor.im.R;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.intro.IntroActivity;

/**
 * Created by longwei on 2016/3/15.
 */
public class TestActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ImageView imageView = (ImageView) findViewById(R.id.test_iv_vector);
        Drawable drawable = imageView.getDrawable();
        if(drawable instanceof Animatable) {

            ((Animatable) drawable).start();


        }
        findViewById(R.id.button_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingDialog dialog = new LoadingDialog();
                dialog.show(getSupportFragmentManager(), "");
            }
        });
    }
}
