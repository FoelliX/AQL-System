package de.foellix.aql.system.storage;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.system.task.TaskAnswer;

public class StorageExplorer {
	int currentId;

	private List<StorageEntry> entries;

	public StorageExplorer() {
		init();
	}

	public void init() {
		this.currentId = 0;
		this.entries = new LinkedList<>();
		for (final String runCmd : Storage.getInstance().getData().getRunAnswerMap().keySet()) {
			this.entries.add(createEntry(runCmd));
		}
	}

	public List<StorageEntry> getSortedById() {
		this.entries.sort(new Comparator<StorageEntry>() {
			@Override
			public int compare(StorageEntry e1, StorageEntry e2) {
				return Integer.valueOf(e1.getId()).compareTo(Integer.valueOf(e2.getId()));
			}
		});
		return this.entries;
	}

	public List<StorageEntry> getSortedByDate() {
		this.entries.sort(new Comparator<StorageEntry>() {
			@Override
			public int compare(StorageEntry e1, StorageEntry e2) {
				return Long.valueOf(e2.getCreationTime()).compareTo(Long.valueOf(e1.getCreationTime()));
			}
		});
		return this.entries;
	}

	public List<StorageEntry> getSortedBySoi() {
		this.entries.sort(new Comparator<StorageEntry>() {
			@Override
			public int compare(StorageEntry e1, StorageEntry e2) {
				return Integer.valueOf(e2.getAnswer().getSubjectOfInterest())
						.compareTo(Integer.valueOf(e1.getAnswer().getSubjectOfInterest()));
			}
		});
		return this.entries;
	}

	public List<StorageEntry> getSortedByFile() {
		this.entries.sort(new Comparator<StorageEntry>() {
			@Override
			public int compare(StorageEntry e1, StorageEntry e2) {
				return e1.getAnswer().getAnswerFile().getAbsolutePath()
						.compareTo(e2.getAnswer().getAnswerFile().getAbsolutePath());
			}
		});
		return this.entries;
	}

	public void deleteById(int id) {
		StorageEntry toRemove = null;
		for (final StorageEntry se : this.entries) {
			if (se.getId() == id) {
				Storage.getInstance().getData().getRunAnswerMap().remove(se.getRunCmd());
				toRemove = se;
				break;
			}
		}
		if (toRemove != null) {
			this.entries.remove(toRemove);
		}

		Storage.getInstance().saveData();
	}

	public void deleteByDate(long timestamp) {
		deleteByDate(timestamp, false);
	}

	public void deleteByDate(long timestamp, boolean millisGiven) {
		if (!millisGiven) {
			timestamp += 999;
		}
		final List<StorageEntry> toRemove = new LinkedList<>();
		for (final StorageEntry se : this.entries) {
			if (se.getCreationTime() <= timestamp) {
				Storage.getInstance().getData().getRunAnswerMap().remove(se.getRunCmd());
				toRemove.add(se);
			}
		}
		this.entries.removeAll(toRemove);

		Storage.getInstance().saveData();
	}

	public void deleteBySoi(int soi) {
		final List<StorageEntry> toRemove = new LinkedList<>();
		for (final StorageEntry se : this.entries) {
			if (se.getAnswer().getSubjectOfInterest() == soi) {
				Storage.getInstance().getData().getRunAnswerMap().remove(se.getRunCmd());
				toRemove.add(se);
			}
		}
		this.entries.removeAll(toRemove);

		Storage.getInstance().saveData();
	}

	public void deleteByFile(File file) {
		final List<StorageEntry> toRemove = new LinkedList<>();
		for (final StorageEntry se : this.entries) {
			if (se.getAnswer().getAnswerFile().getAbsolutePath().equals(file.getAbsolutePath())) {
				Storage.getInstance().getData().getRunAnswerMap().remove(se.getRunCmd());
				toRemove.add(se);
			}
		}
		this.entries.removeAll(toRemove);

		Storage.getInstance().saveData();
	}

	public void delete(StorageEntry se) {
		Storage.getInstance().getData().getRunAnswerMap().remove(se.getRunCmd());
		this.entries.remove(se);

		Storage.getInstance().saveData();
	}

	public void deleteAll() {
		Storage.getInstance().getData().getRunAnswerMap().clear();
		this.entries.clear();

		Storage.getInstance().saveData();
	}

	private StorageEntry createEntry(String runCmd) {
		final TaskAnswer ta = Storage.getInstance().getData().getRunAnswerMap().get(runCmd);
		return new StorageEntry(this, runCmd, ta,
				FileHelper.getFileProperties(ta.getAnswerFile()).creationTime().toMillis());
	}
}