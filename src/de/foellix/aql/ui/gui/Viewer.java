package de.foellix.aql.ui.gui;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.ui.gui.viewer.graph.ViewerGraph;
import de.foellix.aql.ui.gui.viewer.web.ViewerWeb;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class Viewer extends BorderPane {
	private final IGUI parent;

	public MenubarViewer menubarViewer;
	public ViewerXML viewerXML;
	public ViewerGraph viewerGraph;
	public ViewerWeb viewerWeb;

	public Viewer(final IGUI parent) {
		this.parent = parent;

		this.menubarViewer = new MenubarViewer(this);
		this.viewerXML = new ViewerXML(this.menubarViewer);
		this.viewerGraph = new ViewerGraph(this, this.menubarViewer);
		this.viewerWeb = new ViewerWeb(this.menubarViewer);

		parent.getSystem().getAnswerReceivers().add(this.viewerXML);
		parent.getSystem().getAnswerReceivers().add(this.viewerGraph);
		parent.getSystem().getAnswerReceivers().add(this.viewerWeb);

		final TabPane innerTabPane = new TabPane();
		final Tab tabViewer1 = new Tab("XML");
		tabViewer1.setContent(this.viewerXML);
		tabViewer1.setClosable(false);
		final Tab tabViewer2 = new Tab("Graph");
		tabViewer2.setContent(this.viewerGraph);
		tabViewer2.setClosable(false);
		final Tab tabViewer3 = new Tab("Web");
		tabViewer3.setContent(this.viewerWeb);
		tabViewer3.setClosable(false);
		innerTabPane.getTabs().addAll(tabViewer1, tabViewer2, tabViewer3);

		this.setTop(this.menubarViewer);
		if (parent instanceof GUI) {
			final BottomViewer bottomViewer = new BottomViewer(this);
			parent.getSystem().getAnswerReceivers().add(bottomViewer);
			this.setBottom(bottomViewer);
		}
		this.setCenter(innerTabPane);
	}

	public void openFile(final File file) {
		final Answer temp = AnswerHandler.parseXML(file);
		openAnswer(temp);
	}

	public void openAnswer(Answer answer) {
		this.viewerXML.answerAvailable(answer, KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
		this.viewerGraph.answerAvailable(answer, KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
		this.viewerWeb.answerAvailable(answer, KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
	}

	public void refreshGraph() {
		final Answer temp = AnswerHandler.parseXML(this.viewerXML.getContent());
		this.viewerGraph.answerAvailable(temp, KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
		this.viewerWeb.answerAvailable(temp, KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
	}

	public IGUI getParentGUI() {
		return this.parent;
	}

	public void rotate() {
		this.viewerGraph.rotate();
	}

	public void zoomReset() {
		this.viewerGraph.zoomReset();
	}

	public void zoomIn() {
		this.viewerGraph.zoomIn();
	}

	public void zoomOut() {
		this.viewerGraph.zoomOut();
	}

	public void resetContent() {
		this.viewerXML.resetContent();
		this.viewerGraph.answerAvailable(new Answer(), KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
		this.viewerWeb.answerAvailable(new Answer(), KeywordsAndConstants.ANSWER_STATUS_UNKNOWN);
	}
}
