package com.moor.im.options.intro;

import android.content.Intent;
import android.os.Bundle;

import com.moor.im.options.intro.fragment.FirstIntroFragment;
import com.moor.im.options.intro.fragment.SecondIntroFragment;
import com.moor.im.options.intro.fragment.ThirdIntroFragment;
import com.moor.im.options.login.LoginActivity;

/**
 * Created by long on 2015/7/8.
 * 引导页
 */
public class IntroActivity extends AppIntro2{

    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(new FirstIntroFragment(), getApplicationContext());
        addSlide(new SecondIntroFragment(), getApplicationContext());
        addSlide(new ThirdIntroFragment(), getApplicationContext());
    }

    private void loadLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        loadLoginActivity();
    }

}
