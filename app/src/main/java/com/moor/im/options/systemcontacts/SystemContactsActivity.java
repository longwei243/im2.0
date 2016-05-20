package com.moor.im.options.systemcontacts;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.AndroidCharacter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.PingYinUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.easyrecyclerview.recyclerview.EasyRecyclerView;
import com.moor.im.common.views.recyclerviewsidebar.EasyFloatingImageView;
import com.moor.im.common.views.recyclerviewsidebar.EasyRecyclerViewSidebar;
import com.moor.im.common.views.recyclerviewsidebar.sections.EasyImageSection;
import com.moor.im.common.views.recyclerviewsidebar.sections.EasySection;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.contacts.utils.PinyinComparator;
import com.moor.im.options.systemcontacts.adapter.SystemContactAdapter;
import com.moor.im.options.systemcontacts.model.ContactBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/5/13.
 */
public class SystemContactsActivity extends BaseActivity implements EasyRecyclerViewSidebar.OnTouchSectionListener {

    private EasyRecyclerViewSidebar imageSidebar;
    private TextView imageFloatingTv;
    private EasyFloatingImageView imageFloatingIv;

    private EasyRecyclerView imageSectionRv;
    private SystemContactAdapter adapter;
    private Comparator pinyinComparator;

    private Map<Integer, ContactBean> contactIdMap = null;
    private List<ContactBean> list;
    private String contentContact = "";
    private AsyncQueryHandler asyncQueryHandler;

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_contacts);
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("手机联系人");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        pinyinComparator = new SCPinyinComparator();
        this.imageSectionRv = (EasyRecyclerView) findViewById(R.id.section_rv);
        this.imageSidebar = (EasyRecyclerViewSidebar) findViewById(R.id.section_sidebar);
        this.imageFloatingTv = (TextView) findViewById(R.id.section_floating_tv);
        this.imageFloatingIv = (EasyFloatingImageView) findViewById(R.id.section_floating_iv);
        RelativeLayout imageFloatingRl = (RelativeLayout) findViewById(
                R.id.section_floating_rl);
        adapter = new SystemContactAdapter();
        if (this.imageSectionRv != null) {
            this.imageSectionRv.setAdapter(adapter);
        }
        this.imageSidebar.setFloatView(imageFloatingRl);
        this.imageSidebar.setOnTouchSectionListener(this);

        asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
        editText = (EditText) findViewById(R.id.editTextId_ContactList);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                contentContact = editText.getText().toString();
                asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
                init2(contentContact);

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });
        checkPremission();

    }

    private void init() {
        getData();
    }

    private void init2(String inptxt) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY }; // 查询的列
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(inptxt);
        if (m.find()) {
            // System.out.println("进入了字母查询");
            asyncQueryHandler.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
        } else {
            // System.out.println("没有进入字母查询");
            asyncQueryHandler.startQuery(0, null, uri, projection, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + inptxt + "%' or " + ContactsContract.CommonDataKinds.Phone.NUMBER
                    + " like '%" + inptxt + "%'", null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询

        }
    }

    private void getData() {
        showLoadingDialog();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
        asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc");
    }



    @Override public void onTouchImageSection(int sectionIndex, EasyImageSection imageSection) {
        this.imageFloatingTv.setVisibility(View.INVISIBLE);
        this.imageFloatingIv.setVisibility(View.VISIBLE);
        switch (imageSection.imageType) {
            case EasyImageSection.CIRCLE:
                this.imageFloatingIv.setImageType(EasyFloatingImageView.CIRCLE);
                break;
            case EasyImageSection.ROUND:
                this.imageFloatingIv.setImageType(EasyFloatingImageView.ROUND);
                break;
        }
        GlideUtils.displayNative(this.imageFloatingIv, imageSection.resId);
        this.scrollToPosition(this.adapter.getPositionForSection(sectionIndex));
    }


    /**
     * On touch letter section
     *
     * @param sectionIndex sectionIndex
     * @param letterSection letterSection
     */
    @Override public void onTouchLetterSection(int sectionIndex, EasySection letterSection) {
        this.imageFloatingTv.setVisibility(View.VISIBLE);
        this.imageFloatingIv.setVisibility(View.INVISIBLE);
        this.imageFloatingTv.setText(letterSection.letter);
        this.scrollToPosition(this.adapter.getPositionForSection(sectionIndex));
    }


    private void scrollToPosition(int position) {
        this.imageSectionRv.getLinearLayoutManager().scrollToPositionWithOffset(position, 0);
    }

    private void setData(final List<ContactBean> contactBeanList) {
        Observable.create(new Observable.OnSubscribe<List<ContactBean>>() {
            @Override
            public void call(Subscriber<? super List<ContactBean>> subscriber) {
                subscriber.onNext(contactBeanList);
                subscriber.onCompleted();
            }
        })
            .map(new Func1<List<ContactBean>, List<ContactBean>>() {
                @Override
                public List<ContactBean> call(List<ContactBean> contactBeen) {
                    return processContactsData(contactBeen);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<ContactBean>>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    dismissLoadingDialog();
                    Toast.makeText(SystemContactsActivity.this, "获取手机联系人失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNext(List<ContactBean> contactBeen) {
                    initViews(contactBeen);
                }
            });
    }

    private void initViews(List<ContactBean> contactBeen) {
        dismissLoadingDialog();
        this.adapter.setList(contactBeen);
        this.adapter.notifyDataSetChanged();
        this.imageSidebar.setSections(adapter.getSections());
    }

    private List<ContactBean> processContactsData(List<ContactBean> contactsList) {
        List<ContactBean> newContactList = new ArrayList<>();
        for (int i=0; i<contactsList.size(); i++) {
            ContactBean c = contactsList.get(i);
            c.top = false;
            c.setPinyin(PingYinUtil.getPingYin(c.getDesplayName()));
            if (TextUtils.isEmpty(c.getPinyin()) || Character.isDigit(c.getPinyin().charAt(0))) {
                c.header = "#";
            } else {
                c.header = c.getPinyin().substring(0, 1).toUpperCase();
                char temp = c.header.charAt(0);
                if (temp < 'A' || temp > 'Z') {
                    c.header = "#";
                }
            }
            newContactList.add(c);
        }
        Collections.sort(newContactList, pinyinComparator);
        return newContactList;
    }
    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            contactIdMap = new HashMap<>();
            list = new ArrayList<>();
            if (cursor != null && cursor.getCount() > 0) {
                Pattern p = Pattern.compile("[a-zA-Z]");
                Matcher m = p.matcher(contentContact);
                if (m.find()) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {

                        cursor.moveToPosition(i);
                        String name = cursor.getString(1);
                        String number = cursor.getString(2);
                        String sortKey = cursor.getString(3);
                        int contactId = cursor.getInt(4);
                        Long photoId = cursor.getLong(5);
                        String lookUpKey = cursor.getString(6);

                        if (PingYinUtil.getFirstSpell(name).contains(contentContact.toLowerCase())) {
                            ContactBean cb = new ContactBean();

                            cb.setDesplayName(name);
                            if (number.contains(" ")) {
                                number = number.replace(" ", "");
                            }

                            if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
                                cb.setPhoneNum(number.substring(3));
                            } else {
                                cb.setPhoneNum(number);
                            }
                            cb.setSortKey(sortKey);
                            cb.setContactId(contactId);
                            cb.setPhotoId(photoId);
                            cb.setLookUpKey(lookUpKey);
                            list.add(cb);

                            contactIdMap.put(contactId, cb);
                        }
                    }
                } else {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        String name = cursor.getString(1);
                        String number = cursor.getString(2);
                        String sortKey = cursor.getString(3);
                        int contactId = cursor.getInt(4);
                        Long photoId = cursor.getLong(5);
                        String lookUpKey = cursor.getString(6);

                        ContactBean cb = new ContactBean();
                        cb.setDesplayName(name);
                        if (number.contains(" ")) {
                            number = number.replace(" ", "");
                        }
                        if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
                            cb.setPhoneNum(number.substring(3));
                        } else {
                            cb.setPhoneNum(number);
                        }
                        cb.setSortKey(sortKey);
                        cb.setContactId(contactId);
                        cb.setPhotoId(photoId);
                        cb.setLookUpKey(lookUpKey);
                        list.add(cb);
                        contactIdMap.put(contactId, cb);
                    }
                }
            }
            setData(list);
            if(cursor != null) {
                cursor.close();
            }

        }

    }

    private void checkPremission() {
        if(Build.VERSION.SDK_INT < 23) {
            init();
        }else {
            //6.0
            if(ContextCompat.checkSelfPermission(SystemContactsActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                //该权限已经有了
                init();
            }else {
                //申请该权限
                ActivityCompat.requestPermissions(SystemContactsActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 0x2222);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0x2222:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                }else {
                    finish();
                }
                break;
        }
    }
}
