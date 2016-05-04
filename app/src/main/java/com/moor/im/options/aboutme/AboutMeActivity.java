package com.moor.im.options.aboutme;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.options.base.BaseActivity;

/**
 * Created by long on 2015/7/30.
 */
public class AboutMeActivity extends BaseActivity{

    TextView aboutme_tv_version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutme);
        aboutme_tv_version = (TextView) findViewById(R.id.aboutme_tv_version);
        String versionStr = "容联七陌v"+getVersion();
        aboutme_tv_version.setText(versionStr);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("关于我们");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 获取应用版本号
     * @return
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
