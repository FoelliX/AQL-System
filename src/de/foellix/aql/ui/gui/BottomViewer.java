package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.system.IAnswerAvailable;
import javafx.event.EventHandler;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

public class BottomViewer extends TitledPane implements IAnswerAvailable {
	private final Map<TreeItem<String>, File> map;

	private static final TreeView<String> treeView = new TreeView<>();
	private final TreeItem<String> child1, child2, child3;

	BottomViewer(final Viewer parent) {
		super("Answer-Explorer", treeView);

		this.map = new HashMap<>();

		treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (BottomViewer.this.map.get(treeView.getSelectionModel().getSelectedItem()) != null) {
						parent.openFile(BottomViewer.this.map.get(treeView.getSelectionModel().getSelectedItem()));
					}
				}
			}
		});

		final TreeItem<String> root = new TreeItem<>("Answers");
		root.setExpanded(true);

		this.child1 = new TreeItem<>("Last saved Answers");
		root.getChildren().add(this.child1);
		this.child2 = new TreeItem<>("Automatically saved Answers");
		root.getChildren().add(this.child2);
		this.child3 = new TreeItem<>("Partial Answers in Storage");
		root.getChildren().add(this.child3);

		treeView.setRoot(root);
		treeView.setShowRoot(false);
		this.setExpanded(false);
		this.setContent(treeView);

		refresh();
	}

	public void refresh() {
		this.map.clear();
		this.child1.getChildren().clear();
		this.child2.getChildren().clear();
		this.child3.getChildren().clear();

		final FileFilter xmlFilter = new FileFilter() {
			@Override
			public boolean accept(final File file) {
				if (file.getName().length() > 4
						&& file.getName().substring(file.getName().length() - 4).equals(".xml")) {
					return true;
				} else {
					return false;
				}
			}
		};

		for (final File answer : Storage.getInstance().getLastFiles(true)) {
			final TreeItem<String> child = new TreeItem<>(answer.getName());
			this.map.put(child, answer);
			this.child1.getChildren().add(child);
		}

		final File answerFolder = new File("answers/");
		answerFolder.mkdirs();
		for (final File answer : answerFolder.listFiles(xmlFilter)) {
			final TreeItem<String> child = new TreeItem<>(answer.getName());
			this.map.put(child, answer);
			this.child2.getChildren().add(child);
		}

		final File storageFolder = new File("data/storage/");
		storageFolder.mkdirs();
		for (final File answer : storageFolder.listFiles(xmlFilter)) {
			final TreeItem<String> child = new TreeItem<>(answer.getName());
			this.map.put(child, answer);
			this.child3.getChildren().add(child);
		}
	}

	@Override
	public void answerAvailable(Answer answer, int status) {
		refresh();
	}
}
