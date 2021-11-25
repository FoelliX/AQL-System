package de.foellix.aql.ui.gui;

import de.foellix.aql.helper.GUIHelper;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.system.storage.StorageEntry;
import de.foellix.aql.system.storage.StorageExplorer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StorageOverview extends TitledPane implements IAnswerAvailable {
	private static final int SORT_BY_ID = 0;
	private static final int SORT_BY_SOI = 1;
	private static final int SORT_BY_DATE = 2;
	private static final int SORT_BY_FILE = 3;

	private final StorageExplorer storageExplorer;
	private final ListView<StorageEntry> listView;
	private TextArea details;
	private TextField search;
	private CheckBox searchInAnswer;
	private int currentSort;
	private FilteredList<StorageEntry> sortedData;

	StorageOverview(final Viewer parent) {
		super();

		this.storageExplorer = new StorageExplorer();

		this.listView = new ListView<>();
		this.listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (StorageOverview.this.listView.getSelectionModel().getSelectedItem() != null) {
				StorageOverview.this.details
						.setText(StorageOverview.this.listView.getSelectionModel().getSelectedItem().getRunCmd());
			}
		});
		this.listView.setOnMouseClicked((e) -> {
			if (e.getClickCount() == 2) {
				parent.openFile(StorageOverview.this.listView.getSelectionModel().getSelectedItem().getAnswer()
						.getAnswerFile());
			}
		});
		this.listView.setOnKeyPressed((e) -> {
			if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
				parent.openFile(StorageOverview.this.listView.getSelectionModel().getSelectedItem().getAnswer()
						.getAnswerFile());
			} else if (e.getCode() == KeyCode.DELETE) {
				for (final StorageEntry entry : this.listView.getSelectionModel().getSelectedItems()) {
					this.storageExplorer.delete(entry);
				}
				refresh();
			}
		});
		this.listView.setPrefHeight(200);
		this.listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		this.details = new TextArea("Please select an entry.");
		this.details.setEditable(false);
		this.details.setWrapText(true);
		this.details.setPrefHeight(125);

		final VBox box = new VBox();
		box.getChildren().addAll(new Label("Run command:"), this.details);
		box.setPadding(new Insets(5, 0, 0, 0));

		final HBox options = new HBox(5);
		options.setPadding(new Insets(0, 0, 5, 0));
		this.search = new TextField();
		this.search.setPadding(new Insets(2));
		this.search.textProperty().addListener(obs -> {
			filter();
		});
		HBox.setHgrow(this.search, Priority.ALWAYS);
		this.searchInAnswer = new CheckBox();
		this.searchInAnswer.setTooltip(
				new Tooltip("WARNING: For huge answers enabling this option may cause performance issues!"));
		this.searchInAnswer.setSelected(false);
		this.searchInAnswer.setOnAction((e) -> filter());
		final RadioButton sortById = new RadioButton("ID");
		sortById.setUserData(SORT_BY_ID);
		final RadioButton sortBySoi = new RadioButton("Subject of Interest");
		sortBySoi.setUserData(SORT_BY_SOI);
		final RadioButton sortByDate = new RadioButton("Date");
		sortByDate.setUserData(SORT_BY_DATE);
		final RadioButton sortByFile = new RadioButton("File");
		sortByFile.setUserData(SORT_BY_FILE);
		final ToggleGroup sorting = new ToggleGroup();
		sortById.setToggleGroup(sorting);
		sortBySoi.setToggleGroup(sorting);
		sortByDate.setToggleGroup(sorting);
		sortByFile.setToggleGroup(sorting);
		sortById.setSelected(true);
		sorting.selectedToggleProperty().addListener((obj, old, current) -> {
			if (current.getUserData() != null) {
				this.currentSort = (int) current.getUserData();
				refresh();
			}
		});
		options.getChildren().addAll(new Label("Search for:"), this.search, new Label("also in answer:"),
				this.searchInAnswer, new Separator(Orientation.VERTICAL), new Label("Sort by:"), sortById, sortBySoi,
				sortByDate, sortByFile);

		final BorderPane pane = new BorderPane();
		pane.setTop(options);
		pane.setCenter(this.listView);
		pane.setBottom(box);
		GUIHelper.makeResizableInHeight(pane, 350);

		setText("Storage-Overview");
		setExpanded(false);
		setContent(pane);

		this.currentSort = SORT_BY_ID;
		refresh();
	}

	private void filter() {
		final String needle = this.search.getText().toLowerCase();
		if (needle == null || needle.length() == 0) {
			this.sortedData.setPredicate(s -> true);
		} else {
			if (this.searchInAnswer.isSelected()) {
				this.sortedData.setPredicate(
						s -> s.toString().toLowerCase().contains(needle) || s.getRunCmd().toLowerCase().contains(needle)
								|| s.getAnswer().getRawContent().toLowerCase().contains(needle));
			} else {
				this.sortedData.setPredicate(s -> s.toString().toLowerCase().contains(needle)
						|| s.getRunCmd().toLowerCase().contains(needle));
			}
		}
		this.listView.setItems(this.sortedData);
	}

	public void refresh() {
		this.storageExplorer.init();
		final ObservableList<StorageEntry> list = FXCollections.observableArrayList();
		switch (this.currentSort) {
			case SORT_BY_SOI: {
				list.addAll(this.storageExplorer.getSortedBySoi());
				break;
			}
			case SORT_BY_DATE: {
				list.addAll(this.storageExplorer.getSortedByDate());
				break;
			}
			case SORT_BY_FILE: {
				list.addAll(this.storageExplorer.getSortedByFile());
				break;
			}
			default: {
				list.addAll(this.storageExplorer.getSortedById());
			}
		}
		this.sortedData = new FilteredList<>(list);
		Platform.runLater(() -> filter());
	}

	@Override
	public void answerAvailable(Object answer, int status) {
		refresh();
	}
}
