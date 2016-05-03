package com.moor.im.options.setup.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.RequestUrl;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.event.UserInfoUpdate;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.setup.view.ClipImageLayout;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by long on 2015/7/6.
 */
public class ClipImageViewActivity extends BaseActivity{

    ClipImageLayout clipImageLayout;
    Drawable drawable;

    User user = UserDao.getInstance().getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipimageview);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        LogUtil.d("ClipImageViewActivity", "传过来的图片路径是:"+imagePath);

        clipImageLayout = (ClipImageLayout) findViewById(R.id.clipimagelayout);
        final Bitmap bitmap = optimizeBitmap(imagePath, 800, 800);
        drawable = new BitmapDrawable(bitmap);

        clipImageLayout.setmDrawable(drawable);


        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("裁剪头像");
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
                showLoadingDialog();
                //点了确定就上传吧
                Bitmap clipBitmap = clipImageLayout.clip();
                ProcessBitmapTask pbt = new ProcessBitmapTask();
                pbt.execute(clipBitmap);
            }
        });

        LogUtil.d("ClipImageViewActivity", "把图像设置到了裁剪区域");
    }

    public Bitmap optimizeBitmap(String pathName, int maxWidth,
                                        int maxHeight) {
        Bitmap result = null;
        // 图片配置对象，该对象可以配置图片加载的像素获取个数
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 表示加载图像的原始宽高
        options.inJustDecodeBounds = true;
        result = BitmapFactory.decodeFile(pathName, options);
        // Math.ceil表示获取与它最近的整数（向上取值 如：4.1->5 4.9->5）
        int widthRatio = (int) Math.ceil(options.outWidth / maxWidth);
        int heightRatio = (int) Math.ceil(options.outHeight / maxHeight);

        // 设置最终加载的像素比例，表示最终显示的像素个数为总个数的
        if (widthRatio > 1 || heightRatio > 1) {
            if (widthRatio > heightRatio) {
                options.inSampleSize = widthRatio;
            } else {
                options.inSampleSize = heightRatio;
            }
        }
        // 解码像素的模式，在该模式下可以直接按照option的配置取出像素点
        options.inJustDecodeBounds = false;
        result = BitmapFactory.decodeFile(pathName, options);
        return result;
    }


    class UploadFileResponseHandler implements ResponseListener {
        String filePath;

        public UploadFileResponseHandler(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void onFailed() {

            dismissLoadingDialog();
        }

        @Override
        public void onSuccess(String responseString) {
            // TODO Auto-generated method stub
            if (HttpParser.getSucceed(responseString)) {

                String upToken = HttpParser.getUpToken(responseString);
                // qiniu SDK自带方法上传
                UploadManager uploadManager = new UploadManager();
//                final String imgFileKey = "UserIcon/"+UUID.randomUUID().toString();
                //{account}/{type}/{data}/{timestamp}/{filename}
                String fileName = UUID.randomUUID().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                String date = sdf.format(new Date());
                final String imgFileKey = user.account+"/icon/"+date + "/"+ System.currentTimeMillis()+"/"+fileName;
                uploadManager.put(filePath, imgFileKey, upToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key,
                                                 ResponseInfo info, JSONObject response) {
                                // TODO Auto-generated method stub
                                System.out.println("上传头像图片成功了");
                                System.out.println(key + "     " + info
                                        + "      " + response);

                                String iconUrl = RequestUrl.QiniuHttp + imgFileKey;
                                LogUtil.d("UploadFileResponseHandler", "头像在7牛服务器的url:"+iconUrl);

                                HttpManager.getInstance().updateUserIcon(InfoDao.getInstance().getConnectionId(), iconUrl, new UpdateUserIconHandler());
                                LogUtil.d("ClipImageViewActivity", "发起了上传头像的http请求");
                            }
                        }, null);

            }else {
                Toast.makeText(ClipImageViewActivity.this, "网络有问题", Toast.LENGTH_SHORT).show();
                dismissLoadingDialog();
            }
        }
    }


    class UpdateUserIconHandler implements ResponseListener {
        @Override
        public void onFailed() {
            dismissLoadingDialog();
        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {
               dismissLoadingDialog();
                LogUtil.d("UpdateUserIconHandler", "头像修改成功了");
                RxBus.getInstance().send(new UserInfoUpdate());
                ClipImageViewActivity.this.finish();

            } else {
                LogUtil.d("UpdateUserIconHandler", "头像修改失败了");
                dismissLoadingDialog();
                Toast.makeText(ClipImageViewActivity.this, "头像上传失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class ProcessBitmapTask extends AsyncTask {

        @Override
        protected String doInBackground(Object[] params) {
            Bitmap bitmap = (Bitmap) params[0];
            String dirStr = Environment.getExternalStorageDirectory() + File.separator + "m7/iconfile/";

            File dir = new File(dirStr);
            if(!dir.exists()) {
                dir.mkdirs();

            }

            File file = new File(dir, UUID.randomUUID().toString() + "usericon.png");

            String filePath = file.getAbsolutePath();
            OutputStream os = null;
            try {
                os = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return filePath;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            String filePath = (String)o;
            //上传7牛
            HttpManager.getInstance().getQiNiuToken(InfoDao.getInstance().getConnectionId(),
                    filePath, new UploadFileResponseHandler(filePath));

        }
    }
}
