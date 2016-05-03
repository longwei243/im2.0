package com.moor.im.common.utils;

import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.utils.log.LogUtil;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by longwei on 2016/4/26.
 */
public class ObservableUtils {

    /**
     * 从数据库获取联系人
     * @return
     */
    public static Observable<List<Contacts>> getContactsFormDB() {

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
}
