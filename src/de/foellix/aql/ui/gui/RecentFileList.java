package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

public class RecentFileList extends LinkedList<File> implements Serializable {
	private static final long serialVersionUID = -5816292434960057538L;

	private static final int SIZE = 10;

	public List<File> getRecentFiles() {
		return Lists.reverse(this);
	}

	@Override
	public boolean add(File file) {
		if (this.contains(file)) {
			super.remove(file);
			return super.add(file);
		} else {
			while (size() >= SIZE) {
				this.removeFirst();
			}
			return super.add(file);
		}
	}
}