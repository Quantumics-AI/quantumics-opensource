package com.qs.api.util;

import java.util.Comparator;

import com.qs.api.model.QsFiles;

public class FileComparator implements Comparator<QsFiles> {
	public int compare(QsFiles o1, QsFiles o2) {
		return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	}
}
