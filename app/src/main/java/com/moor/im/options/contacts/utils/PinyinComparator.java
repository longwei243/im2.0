package com.moor.im.options.contacts.utils;

import com.moor.im.common.model.Contacts;

import java.util.Comparator;

/**
 *
 */
public class PinyinComparator implements Comparator<Contacts> {

	@Override
	public int compare(Contacts o1, Contacts o2) {
		if (o2.header.equals("#")) {
			return -1;
		} else if (o1.header.equals("#")) {
			return -1;
		} else {
			return o1.header.compareTo(o2.header);
		}
	}

}
