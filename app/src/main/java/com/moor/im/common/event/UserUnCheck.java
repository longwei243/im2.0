package com.moor.im.common.event;

import com.moor.im.common.model.Contacts;

/**
 * Created by longwei on 2016/4/26.
 */
public class UserUnCheck {

    public Contacts contacts;

    public UserUnCheck(Contacts contacts) {
        this.contacts = contacts;
    }
}
