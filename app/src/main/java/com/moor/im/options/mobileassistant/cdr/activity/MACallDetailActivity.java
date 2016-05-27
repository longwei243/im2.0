package com.moor.im.options.mobileassistant.cdr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.dial.dialog.CallChoiseDialog;
import com.moor.im.options.mobileassistant.cdr.view.MP3PlayerView;
import com.moor.im.options.mobileassistant.model.MACallLogData;


/**
 * Created by longwei on 2016/2/24.
 */
public class MACallDetailActivity extends BaseActivity{

    private MACallLogData callLogData;

    private TextView mycalldetail_tv_callno, mycalldetail_tv_calledno,
            mycalldetail_tv_province, mycalldetail_tv_district,
            mycalldetail_tv_investigate, mycalldetail_tv_offeringtime,
            mycalldetail_tv_begintime, mycalldetail_tv_timelength,
            mycalldetail_tv_statusdesc, mycalldetail_tv_agent,mycalldetail_tv_queue,
            mycalldetail_tv_status;

    private ImageView mycalldetail_iv_status;

    private Button mycalldetail_btn_call;

    private MP3PlayerView mycalldetail_mp3player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macalldetail);

        Intent intent = getIntent();
        if(intent.getSerializableExtra("calllogdata") != null) {
            callLogData = (MACallLogData) intent.getSerializableExtra("calllogdata");
        }
        initViews();
        initData();

    }

    private void initViews() {

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("通话详情");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mycalldetail_tv_callno = (TextView) findViewById(R.id.mycalldetail_tv_callno);
        mycalldetail_tv_calledno = (TextView) findViewById(R.id.mycalldetail_tv_calledno);
        mycalldetail_tv_province = (TextView) findViewById(R.id.mycalldetail_tv_province);
        mycalldetail_tv_district = (TextView) findViewById(R.id.mycalldetail_tv_district);
        mycalldetail_tv_investigate = (TextView) findViewById(R.id.mycalldetail_tv_investigate);
        mycalldetail_tv_offeringtime = (TextView) findViewById(R.id.mycalldetail_tv_offeringtime);
        mycalldetail_tv_begintime = (TextView) findViewById(R.id.mycalldetail_tv_begintime);
        mycalldetail_tv_timelength = (TextView) findViewById(R.id.mycalldetail_tv_timelength);
        mycalldetail_tv_statusdesc = (TextView) findViewById(R.id.mycalldetail_tv_statusdesc);
        mycalldetail_tv_agent = (TextView) findViewById(R.id.mycalldetail_tv_agent);
        mycalldetail_tv_queue = (TextView) findViewById(R.id.mycalldetail_tv_queue);
        mycalldetail_tv_status = (TextView) findViewById(R.id.mycalldetail_tv_status);
        mycalldetail_iv_status = (ImageView) findViewById(R.id.mycalldetail_iv_status);


        mycalldetail_mp3player = (MP3PlayerView) findViewById(R.id.mycalldetail_mp3player);
        mycalldetail_btn_call = (Button) findViewById(R.id.mycalldetail_btn_call);

    }

    private void initData() {
        if(callLogData == null) {
            return;
        }
        mycalldetail_tv_callno.setText(NullUtil.checkNull(callLogData.CALL_NO));
        mycalldetail_tv_calledno.setText(NullUtil.checkNull(callLogData.CALLED_NO));
        mycalldetail_tv_province.setText(NullUtil.checkNull(callLogData.PROVINCE));
        mycalldetail_tv_district.setText(NullUtil.checkNull(callLogData.DISTRICT));
        mycalldetail_tv_investigate.setText(NullUtil.checkNull(callLogData.INVESTIGATE));
        mycalldetail_tv_offeringtime.setText(NullUtil.checkNull(callLogData.OFFERING_TIME));
        mycalldetail_tv_begintime.setText(NullUtil.checkNull(callLogData.BEGIN_TIME));
        if(!"0秒".equals(NullUtil.checkNull(callLogData.shortCallTimeLength))) {
            mycalldetail_tv_timelength.setText(NullUtil.checkNull(callLogData.shortCallTimeLength));
        }
        mycalldetail_tv_statusdesc.setText(NullUtil.checkNull(callLogData.status));
        mycalldetail_tv_agent.setText(NullUtil.checkNull(callLogData.agent));
        mycalldetail_tv_queue.setText(NullUtil.checkNull(callLogData.queue));

        String dialType = NullUtil.checkNull(callLogData.dialType);
        String number = "";
        if("outbound".equals(dialType)) {
            mycalldetail_tv_status.setText("外呼去电");
            mycalldetail_iv_status.setImageResource(R.drawable.outcall_icon);
            number = NullUtil.checkNull(callLogData.CALLED_NO);
        }else if("inbound".equals(dialType)) {
            mycalldetail_tv_status.setText("接听来电");
            mycalldetail_iv_status.setImageResource(R.drawable.incall_icon);
            number = NullUtil.checkNull(callLogData.CALL_NO);
        }

        final String finalNumber = number;
        mycalldetail_btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MACallDetailActivity.this, CallChoiseDialog.class);
                intent.putExtra(M7Constant.PHONE_NUM, finalNumber);
                startActivity(intent);
            }
        });

        if("success".equals(callLogData.statusClass)) {
            final String urlPath = callLogData.FILE_SERVER+"/"+callLogData.RECORD_FILE_NAME;
            mycalldetail_mp3player.setVisibility(View.VISIBLE);
            mycalldetail_mp3player.setUrlPath(urlPath);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mycalldetail_mp3player.stop();
    }


}
