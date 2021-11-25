package de.foellix.aql.ui.gui;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.ui.gui.viewer.web.ViewerWeb;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class Viewer extends BorderPane {
	private IGUI parent;

	public boolean ready;

	public MenubarViewer menubarViewer;
	public ViewerXML viewerXML;
	public ViewerWeb viewerWeb;

	public Viewer() {
		this.ready = false;
	}

	public Viewer(final IGUI parent) {
		this();
		init(parent);
	}

	public void init(final IGUI parent) {
		this.parent = parent;

		this.setMaxHeight(Double.MAX_VALUE);
		this.setPrefHeight(Double.MAX_VALUE);

		this.menubarViewer = new MenubarViewer(this);
		this.viewerXML = new ViewerXML(this.menubarViewer);
		this.viewerWeb = new ViewerWeb(this.menubarViewer);

		parent.getSystem().getAnswerReceivers().add(this.viewerXML);
		parent.getSystem().getAnswerReceivers().add(this.viewerWeb);

		final TabPane innerTabPane = new TabPane();
		final Tab tabViewerXML = new Tab("XML");
		tabViewerXML.setContent(this.viewerXML);
		tabViewerXML.setClosable(false);
		final Tab tabViewerWeb = new Tab("Web");
		tabViewerWeb.setContent(this.viewerWeb);
		tabViewerWeb.setClosable(false);
		innerTabPane.getTabs().addAll(tabViewerXML, tabViewerWeb);
		innerTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
				if (newTab == tabViewerXML) {
					Viewer.this.menubarViewer.buttonState(MenubarViewer.MODE_VIEWER_XML);
				} else {
					Viewer.this.menubarViewer.buttonState(MenubarViewer.MODE_VIEWER_WEB);
				}
			}
		});

		Viewer.this.menubarViewer.buttonState(MenubarViewer.MODE_VIEWER_XML);

		this.setTop(this.menubarViewer);
		if (parent instanceof GUI) {
			final StorageOverview bottomViewer = new StorageOverview(this);
			parent.getSystem().getAnswerReceivers().add(bottomViewer);
			this.setBottom(bottomViewer);
		}
		this.setCenter(innerTabPane);
	}

	public boolean isReady() {
		return this.ready;
	}

	public void openFile(final File file) {
		final Answer temp = AnswerHandler.parseXML(file);
		openAnswer(temp);
	}

	public void openAnswer(Answer answer) {
		this.viewerXML.answerAvailable(answer, Task.STATUS_EXECUTION_UNKNOWN);
		this.viewerWeb.answerAvailable(answer, Task.STATUS_EXECUTION_UNKNOWN);
	}

	public void refreshGraph() {
		final Answer temp = AnswerHandler.parseXML(this.viewerXML.getContent());
		this.viewerWeb.answerAvailable(temp, Task.STATUS_EXECUTION_UNKNOWN);
	}

	public IGUI getParentGUI() {
		return this.parent;
	}

	public void inBrowser() {
		this.viewerWeb.inBrowser();
	}

	public void resetContent() {
		this.viewerXML.resetContent();
		this.viewerWeb.answerAvailable(new Answer(), Task.STATUS_EXECUTION_UNKNOWN);
	}
}