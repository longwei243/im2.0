package com.moor.im.options.dial;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.event.DialEvent;
import com.moor.im.common.event.UnReadCount;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.Utils;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.dial.adapter.CallLogAdapter;
import com.moor.im.options.dial.dialog.CallChoiseDialog;
import com.moor.im.options.dial.model.CallLogModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by longwei on 2016/3/16.
 */
public class DialFragment extends BaseLazyFragment {
    /**
     * 拨号盘布局
     */
    private LinearLayout dialplate_layout;

    private ListView calllog_listview;
    CallLogAdapter adapter;

    private EditText editText_phone_number;

    private Button btn_dialnum_1;
    private Button btn_dialnum_2;
    private Button btn_dialnum_3;
    private Button btn_dialnum_4;
    private Button btn_dialnum_5;
    private Button btn_dialnum_6;
    private Button btn_dialnum_7;
    private Button btn_dialnum_8;
    private Button btn_dialnum_9;
    private Button btn_dialnum_0;
    private Button btn_dialnum_delete;
    private Button btn_dialnum_call;


    private AsyncQueryHandler asyncQuery;
    private List<CallLogModel> callLogs;

    private SharedPreferences settings;
    /**
     * 拨号盘高度
     */
    private int dialplate_layout_height;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asyncQuery = new MyAsyncQueryHandler(getActivity().getContentResolver());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dial, null);
        initViews(view);
        settings = getActivity().getSharedPreferences(MobileApplication.getInstance()
                        .getResources().getString(R.string.spname),
                Activity.MODE_PRIVATE);

        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof DialEvent) {
                            final String dia_key = settings.getString("ClickState", "")
                                    .trim();
                            final String dia_t1 = settings.getString("moveState", "")
                                    .trim();
                            if (!dia_t1.equals("STATE_MOVE")) {
                                if (dia_key.equals("STATE_SHOW")) {
                                    dialplateOpen();
                                } else {
                                    dialplateClose();
                                }
                            }
                        }
                    }
                }));
        return view;
    }

    private void initViews(View view) {


        dialplate_layout = (LinearLayout) view
                .findViewById(R.id.dialplate_layout);
        editText_phone_number = (EditText) view
                .findViewById(R.id.dialplate_edittext_phonenum);

        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText_phone_number.setInputType(InputType.TYPE_NULL);
        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setSoftInputShownOnFocus;
                setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setSoftInputShownOnFocus.setAccessible(true);
                setSoftInputShownOnFocus.invoke(editText_phone_number, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        btn_dialnum_1 = (Button) view.findViewById(R.id.dialNum1);
        btn_dialnum_2 = (Button) view.findViewById(R.id.dialNum2);
        btn_dialnum_3 = (Button) view.findViewById(R.id.dialNum3);
        btn_dialnum_4 = (Button) view.findViewById(R.id.dialNum4);
        btn_dialnum_5 = (Button) view.findViewById(R.id.dialNum5);
        btn_dialnum_6 = (Button) view.findViewById(R.id.dialNum6);
        btn_dialnum_7 = (Button) view.findViewById(R.id.dialNum7);
        btn_dialnum_8 = (Button) view.findViewById(R.id.dialNum8);
        btn_dialnum_9 = (Button) view.findViewById(R.id.dialNum9);
        btn_dialnum_0 = (Button) view.findViewById(R.id.dialNum0);
        btn_dialnum_delete = (Button) view.findViewById(R.id.dial_delete);
        btn_dialnum_call = (Button) view.findViewById(R.id.dial_call);

        btn_dialnum_1.setOnClickListener(clickListener);
        btn_dialnum_2.setOnClickListener(clickListener);
        btn_dialnum_3.setOnClickListener(clickListener);
        btn_dialnum_4.setOnClickListener(clickListener);
        btn_dialnum_5.setOnClickListener(clickListener);
        btn_dialnum_6.setOnClickListener(clickListener);
        btn_dialnum_7.setOnClickListener(clickListener);
        btn_dialnum_8.setOnClickListener(clickListener);
        btn_dialnum_9.setOnClickListener(clickListener);
        btn_dialnum_0.setOnClickListener(clickListener);
        btn_dialnum_delete.setOnClickListener(clickListener);
        btn_dialnum_call.setOnClickListener(clickListener);
        btn_dialnum_delete.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                editText_phone_number.setText("");
                return false;
            }
        });
        // 获取拨号盘的高度
        dialplate_layout.measure(0, 0);
        dialplate_layout_height = dialplate_layout.getMeasuredHeight();


        calllog_listview = (ListView) view.findViewById(R.id.dialplate_listview);
        calllog_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                CallLogModel clm = (CallLogModel) parent.getAdapter().getItem(position);
                String number = clm.getNumber();
                Intent intent = new Intent(getActivity(), CallChoiseDialog.class);
                intent.putExtra(M7Constant.PHONE_NUM, number);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        init();
    }

    private void init() {
        Uri uri = android.provider.CallLog.Calls.CONTENT_URI;
        // 查询的列
        String[] projection = { CallLog.Calls.DATE, // 日期
                CallLog.Calls.NUMBER, // 号码
                CallLog.Calls.TYPE, // 类型
                CallLog.Calls.CACHED_NAME, // 名字
                CallLog.Calls.DURATION, // 时长
                CallLog.Calls._ID, // id
        };

        asyncQuery.startQuery(0, null, uri, projection, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER+ " limit "+100);
    }

    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                callLogs = new ArrayList<CallLogModel>();

                cursor.moveToFirst(); // 游标移动到第一项
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    long date = cursor.getLong(cursor
                            .getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor
                            .getColumnIndex(CallLog.Calls.DURATION));
                    String number = cursor.getString(cursor
                            .getColumnIndex(CallLog.Calls.NUMBER));
                    String type = cursor.getString(cursor
                            .getColumnIndex(CallLog.Calls.TYPE));
                    String cachedName = cursor.getString(cursor
                            .getColumnIndex(CallLog.Calls.CACHED_NAME));// 缓存的名称与电话号码，如果它的存在
                    long id = cursor.getLong(cursor
                            .getColumnIndex(CallLog.Calls._ID));

                    CallLogModel callLogBean = new CallLogModel();
                    callLogBean.set_id(id);
                    callLogBean.setNumber(number);
                    callLogBean.setDuration(duration);
                    callLogBean.setDisplayName(cachedName);
                    if (null == cachedName || "".equals(cachedName)) {
                        callLogBean.setDisplayName(number);
                    }
                    String typeStr = "";
                    if(type.equals("1")) {
                        typeStr = "呼入";
                    }else if(type.equals("2")){
                        typeStr = "呼出";
                    }else if(type.equals("3")){
                        typeStr = "未接听";
                    }
                    callLogBean.setType(typeStr);
                    callLogBean.setDate(date);

                    callLogs.add(callLogBean);
                }
                if (callLogs.size() > 0) {
                    setAdapter(callLogs);
                }
                cursor.close();
            }
            super.onQueryComplete(token, cookie, cursor);
        }
    }

    private void setAdapter(List<CallLogModel> callLogs) {
        adapter = new CallLogAdapter(getActivity(), callLogs);
        calllog_listview.setAdapter(adapter);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            StringBuffer sb = new StringBuffer(editText_phone_number.getText()
                    .toString());
            switch (v.getId()) {
                case R.id.dialNum1:
                    sb.append("1");
                    break;
                case R.id.dialNum2:
                    sb.append("2");
                    break;
                case R.id.dialNum3:
                    sb.append("3");
                    break;
                case R.id.dialNum4:
                    sb.append("4");
                    break;
                case R.id.dialNum5:
                    sb.append("5");
                    break;
                case R.id.dialNum6:
                    sb.append("6");
                    break;
                case R.id.dialNum7:
                    sb.append("7");
                    break;
                case R.id.dialNum8:
                    sb.append("8");
                    break;
                case R.id.dialNum9:
                    sb.append("9");
                    break;
                case R.id.dialNum0:
                    sb.append("0");
                    break;
                case R.id.dial_delete:
                    if (editText_phone_number.getText().length() != 0) {
                        //找到光标位置
                        int index = editText_phone_number.getSelectionStart();
                        if(index > 0) {
                            sb.deleteCharAt(index - 1);
                        }

                    }
                    break;
                case R.id.dial_call:
                    if(!"".equals(editText_phone_number.getText().toString().trim())) {
                        Intent intent = new Intent(getActivity(), CallChoiseDialog.class);
                        intent.putExtra(M7Constant.PHONE_NUM, editText_phone_number.getText().toString().trim());
                        startActivity(intent);
                    }else{
                        Toast.makeText(getActivity(), "请输入号码后拨打", Toast.LENGTH_SHORT).show();
                    }

                    break;

                default:
                    break;
            }
            if (sb.toString() != null) {
                editText_phone_number.setText(sb.toString());
                editText_phone_number.setSelection(sb.length());
            }
        }
    };

    /**
     * 拨号盘关闭
     */
    private void dialplateClose() {
        ObjectAnimator
                .ofFloat(dialplate_layout, "translationY", 0,
                        dialplate_layout_height).setDuration(600).start();
    }

    /**
     * 拨号盘打开
     */
    private void dialplateOpen() {
        ObjectAnimator
                .ofFloat(dialplate_layout, "translationY",
                        dialplate_layout_height, 0).setDuration(600).start();
    }

    public void onEventMainThread(DialEvent de) {
        settings = getActivity().getSharedPreferences(
                MobileApplication.getInstance().getResources()
                        .getString(R.string.spname), Activity.MODE_PRIVATE);
        final String dia_key = settings.getString("ClickState", "").toString()
                .trim();
        final String dia_t1 = settings.getString("moveState", "").toString()
                .trim();
        if (!dia_t1.equals("STATE_MOVE")) {
            if (dia_key.equals("") | dia_key.equals("STATE_SHOW")) {
                dialplateOpen();
            } else {
                dialplateClose();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}