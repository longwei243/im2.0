package com.moor.im.options.contacts.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Group;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.moor.im.common.views.easyrecyclerview.recyclerview.EasyRecyclerView;
import com.moor.im.common.views.recyclerviewsidebar.EasyFloatingImageView;
import com.moor.im.common.views.recyclerviewsidebar.EasyRecyclerViewSidebar;
import com.moor.im.common.views.recyclerviewsidebar.sections.EasyImageSection;
import com.moor.im.common.views.recyclerviewsidebar.sections.EasySection;
import com.moor.im.helptest.activity.TestActivity;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.contacts.activity.ContactsDetailActivity;
import com.moor.im.options.contacts.adapter.ContactAdatper;
import com.moor.im.options.contacts.utils.PinyinComparator;
import com.moor.im.options.discussion.activity.DiscussionActivity;
import com.moor.im.options.group.activity.GroupActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/3/16.
 */
public class ContactFragment extends BaseLazyFragment implements EasyRecyclerViewSidebar.OnTouchSectionListener {

    SharedPreferences myPreferences;
    SharedPreferences.Editor editor;

    private EasyRecyclerViewSidebar imageSidebar;
    private TextView imageFloatingTv;
    private EasyFloatingImageView imageFloatingIv;

    private EasyRecyclerView imageSectionRv;
    private ContactAdatper adapter;
    private Comparator pinyinComparator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, null);

        myPreferences = getActivity().getSharedPreferences(MobileApplication.getInstance()
                        .getResources().getString(R.string.spname),
                Activity.MODE_PRIVATE);
        editor = myPreferences.edit();
        pinyinComparator = new PinyinComparator();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        this.imageSectionRv = (EasyRecyclerView) view.findViewById(R.id.section_rv);
        this.imageSidebar = (EasyRecyclerViewSidebar) view.findViewById(R.id.section_sidebar);
        this.imageFloatingTv = (TextView) view.findViewById(R.id.section_floating_tv);
        this.imageFloatingIv = (EasyFloatingImageView) view.findViewById(R.id.section_floating_iv);
        RelativeLayout imageFloatingRl = (RelativeLayout) view.findViewById(
                R.id.section_floating_rl);

        adapter = new ContactAdatper();
        if (this.imageSectionRv != null) {
            this.imageSectionRv.setAdapter(adapter);
        }

        this.imageSidebar.setFloatView(imageFloatingRl);
        this.imageSidebar.setOnTouchSectionListener(this);
    }

    @Override
    public void onFirstUserVisible() {
        //第一次界面显示，加载数据
        getData();
        getContactsVersion();
    }
    /**
     * 填充数据
     */
    private void initData(final List<Contacts> contactsList) {
        this.adapter.setList(contactsList);
        this.adapter.notifyDataSetChanged();
        this.imageSidebar.setSections(adapter.getSections());

        this.adapter.setOnItemClickListener(new EasyRecyclerViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View convertView, int position) {
                if(position == 0) {
                    //群组
                    Intent intent = new Intent(getActivity(),
                            GroupActivity.class);
                    startActivity(intent);
                }else if(position == 1){
                    //讨论组
                    Intent intent = new Intent(getActivity(),
                            DiscussionActivity.class);
                    startActivity(intent);
                }else if(position == 2){
                    //组织架构
                }else if(position == 3){
                    //手机联系人

                }else {
                    Contacts contact = contactsList.get(position);
                    Intent intent = new Intent(getActivity(),
                            ContactsDetailActivity.class);
                    intent.putExtra("contact", contact);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 获取联系人数据
     * @return
     */
    private void getData() {

        Observable<List<Contacts>> observable_netWithSave = getDataFromNetWithSave();
        Observable<List<Contacts>> observable_db = getDataFormDB();

        mCompositeSubscription.add(Observable
                .concat(observable_db, observable_netWithSave)
                .first(new Func1<List<Contacts>, Boolean>() {
                    @Override
                    public Boolean call(List<Contacts> contactsList) {
                        if(contactsList != null && contactsList.size() > 0) {
                            return true;
                        }
                        return false;
                    }
                })
                .map(new Func1<List<Contacts>, List<Contacts>>() {
                    @Override
                    public List<Contacts> call(List<Contacts> contactsList) {
                        return processContactsData(contactsList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Contacts>>() {
                    @Override
                    public void onCompleted() {
                        LogUtil.d("get contacts completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.d("get contacts error");
                    }

                    @Override
                    public void onNext(List<Contacts> contacts) {
                        initData(contacts);
                    }
                }));

    }

    private void refreshFromNetData() {
        mCompositeSubscription.add(getDataFromNetWithSave()
                .map(new Func1<List<Contacts>, List<Contacts>>() {
                    @Override
                    public List<Contacts> call(List<Contacts> contactsList) {
                        return processContactsData(contactsList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Contacts>>() {
                    @Override
                    public void onCompleted() {
                        LogUtil.d("get contacts completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.d("get contacts error");
                    }

                    @Override
                    public void onNext(List<Contacts> contacts) {
                        initData(contacts);
                    }
                })
        );
    }

    /**
     * 获取联系人版本号
     */
    private void getContactsVersion() {
        HttpManager.getInstance().getVersion(InfoDao.getInstance().getConnectionId(), new ResponseListener() {
            @Override
            public void onFailed() {

            }

            @Override
            public void onSuccess(String responseStr) {
                if(HttpParser.getSucceed(responseStr)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Long contactsVersion = jsonObject.getLong("ContactsVersion");
                        if(!"".equals(myPreferences.getString("contactsVersion", ""))) {
                            if(!myPreferences.getString("contactsVersion", "").equals(contactsVersion + "")) {
                                //需更新
                                refreshFromNetData();
                            }
                        }

                        editor.putString("contactsVersion", contactsVersion + "");
                        editor.commit();

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 从数据库获取联系人
     * @return
     */
    private Observable<List<Contacts>> getDataFormDB() {

        return Observable.create(new Observable.OnSubscribe<List<Contacts>>() {
            @Override
            public void call(Subscriber<? super List<Contacts>> subscriber) {
                List<Contacts> contactsList = ContactsDao.getInstance().getContacts();
                if(!subscriber.isUnsubscribed()) {
                    if(contactsList != null) {
                        LogUtil.d("从数据库加载联系人");
                        subscriber.onNext(contactsList);
                        subscriber.onCompleted();
                    }else {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }
                }

            }
        });
    }

    /**
     * 从网络加载数据并保存到数据库
     * @return
     */
    private Observable<List<Contacts>> getDataFromNetWithSave() {
        return HttpManager.getInstance().getContacts(InfoDao.getInstance().getConnectionId())
                .doOnNext(new Action1<List<Contacts>>() {
            @Override
            public void call(List<Contacts> contactsList) {
                ContactsDao.getInstance().saveContacts(contactsList);
            }
        });
    }

    /**
     * 联系人数据进行处理,拼音、排序, 添加最上面数据
     * @return
     */
    private List<Contacts> processContactsData(List<Contacts> contactsList) {
        List<Contacts> newContactList = new ArrayList<>();
        for (int i=0; i<contactsList.size(); i++) {
            Contacts c = contactsList.get(i);
            c.top = false;
            if (TextUtils.isEmpty(c.pinyin) || Character.isDigit(c.pinyin.charAt(0))) {
                c.header = "#";
            } else {
                c.header = c.pinyin.substring(0, 1).toUpperCase();
                char temp = c.header.charAt(0);
                if (temp < 'A' || temp > 'Z') {
                    c.header = "#";
                }
            }
            newContactList.add(c);
        }
        Collections.sort(newContactList, pinyinComparator);

        List<Contacts> datas = new ArrayList<>();
        datas.addAll(initHeaderDatas());
        datas.addAll(newContactList);
        return datas;
    }


    /**
     * 生成最上面的数据
     * @return
     */
    private List<Contacts> initHeaderDatas() {
        List<Contacts> contactsList = new ArrayList<>();
        Contacts first = new Contacts();
        first.displayName = "群组";
        first.top = true;
        first.resId = R.drawable.ic_addfriend_group;
        contactsList.add(first);
        Contacts second = new Contacts();
        second.displayName = "讨论组";
        second.top = true;
        second.resId = R.drawable.ic_addfriend_discuss;
        contactsList.add(second);
        Contacts third = new Contacts();
        third.displayName = "组织架构";
        third.top = true;
        third.resId = R.drawable.ic_addfriend_department;
        contactsList.add(third);
        Contacts four = new Contacts();
        four.displayName = "手机联系人";
        four.top = true;
        four.resId = R.drawable.ic_addfriend_contact;
        contactsList.add(four);
        return contactsList;
    }

    /**
     * On touch image section
     *
     * @param sectionIndex sectionIndex
     * @param imageSection imageSection
     */
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
}
