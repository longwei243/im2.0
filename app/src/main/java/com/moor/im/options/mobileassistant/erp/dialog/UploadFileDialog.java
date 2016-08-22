package com.moor.im.options.mobileassistant.erp.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.options.mobileassistant.erp.activity.ErpActionProcessActivity;
import com.moor.imkf.qiniu.http.ResponseInfo;
import com.moor.imkf.qiniu.storage.UpCompletionHandler;
import com.moor.imkf.qiniu.storage.UpProgressHandler;
import com.moor.imkf.qiniu.storage.UploadManager;
import com.moor.imkf.qiniu.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by longwei on 2016/3/24.
 */
public class UploadFileDialog  extends DialogFragment {
    private User user = UserDao.getInstance().getUser();

    ErpActionProcessActivity.OnFileUploadCompletedListener listener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View uploadView = inflater.inflate(R.layout.erp_field_file_uploading, null);
        TextView tv_fileName = (TextView) uploadView.findViewById(R.id.erp_field_file_upload_tv_filename);
        TextView tv_fileSize = (TextView) uploadView.findViewById(R.id.erp_field_file_upload_tv_filesize);
        TextView tv_precent = (TextView) uploadView.findViewById(R.id.erp_field_file_upload_tv_precent);
        ProgressBar erp_field_file_upload_tv_precent = (ProgressBar) uploadView.findViewById(R.id.erp_field_file_upload_pb);

        Bundle b = getArguments();
        File file = (File) b.getSerializable("file");
        String fileName = b.getString("fileName");
        String fileSizeStr = b.getString("fileSize");
        tv_fileName.setText(fileName);
        tv_fileSize.setText(fileSizeStr);

        Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        dialog.setContentView(uploadView);
        dialog.setCanceledOnTouchOutside(false);
        HttpManager.getInstance().getErpQiNiuToken(
                            new UploadFileResponseHandler(fileName, file, erp_field_file_upload_tv_precent, tv_precent));

        return dialog;
    }


    public void setOnFileUploadCompletedListener(ErpActionProcessActivity.OnFileUploadCompletedListener listener) {
        this.listener = listener;
    }

    /**
     * 上传文件回调
     * @author LongWei
     *
     */
    class UploadFileResponseHandler implements ResponseListener {

        public String fileName;
        public File file;
        public ProgressBar pb;
        public TextView textView;
        public UploadFileResponseHandler(String fileName, File file, ProgressBar pb, TextView textView) {
            this.fileName = fileName;
            this.file = file;
            this.pb = pb;
            this.textView = textView;
        }

        @Override
        public void onFailed() {
            listener.onFailed();
			Toast.makeText(getActivity(), "上传文件失败了", Toast.LENGTH_SHORT).show();;
        }

        @Override
        public void onSuccess(String responseString) {
            // TODO Auto-generated method stub
            System.out.println("上传文件获取token返回数据:"+responseString);
            String upToken = HttpParser.getUpToken(responseString);
            // qiniu SDK自带方法上传
            UploadManager uploadManager = new UploadManager();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
            String date = sdf.format(new Date());
            final String fileKey = user.account+"/business/"+date + "/"+ System.currentTimeMillis()+"/"+ fileName;

            uploadManager.put(file, fileKey, upToken,
                    new UpCompletionHandler() {

                        @Override
                        public void complete(String key,
                                             ResponseInfo info, JSONObject response) {
                            // TODO Auto-generated method stub
//									System.out.println("上传图片成功了");
                            System.out.println(key + "     " + info
                                    + "      " + response);
                            if(listener != null) {
                                listener.onCompleted(fileName, fileKey);
                            }
                            dismiss();

                        }
                    }, new UploadOptions(null, null, false,
                            new UpProgressHandler(){
                                public void progress(String key, final double percent){
                                    Log.i("qiniu", key + ": " + (int) (percent * 100));
                                    pb.setProgress((int) (percent * 100));
                                    textView.setText((int) (percent * 100) + "%");

                                }
                            }, null));

        }
    }

}
