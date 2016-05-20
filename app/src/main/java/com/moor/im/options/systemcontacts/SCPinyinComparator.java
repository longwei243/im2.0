package com.moor.im.options.systemcontacts;

import com.moor.im.common.model.Contacts;
import com.moor.im.options.systemcontacts.model.ContactBean;

import java.util.Comparator;

/**
 *
 */
public class SCPinyinComparator implements Comparator<ContactBean> {

	@Override
	public int compare(ContactBean o1, ContactBean o2) {
		if (o2.header.equals("#")) {
			return -1;
		} else if (o1.header.equals("#")) {
			return 1;
		} else {
			return o1.header.compareTo(o2.header);
		}
	}

}
