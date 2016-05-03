package com.moor.im.options.imageviewlook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.options.imageviewlook.view.TouchImageView;

import java.io.File;
import java.util.UUID;

/**
 * Created by long on 2015/7/3.
 */
public class ImageViewLookActivity extends Activity{

    private TouchImageView touchImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image_look);
        touchImageView = (TouchImageView) findViewById(R.id.matrixImageView);

        Intent intent = getIntent();
        final String imgPath = intent.getStringExtra(M7Constant.IMG_PATH);

        if(imgPath != null && !"".equals(imgPath)) {
            Glide.with(this).load(imgPath)
                    .placeholder(R.drawable.pic_thumb_bg)
                    .error(R.drawable.image_download_fail_icon)
                    .into(touchImageView);
        }else {
            finish();
        }
        touchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
