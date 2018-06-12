package de.foellix.aql.config.wizard;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class Overview extends BorderPane {
	public static final int TYPE_TOOL = 0;
	public static final int TYPE_PREPROCESSOR = 1;
	public static final int TYPE_OPERATOR = 2;
	public static final int TYPE_CONVERTER = 3;

	private final ConfigWizard parent;

	EditorOverview editorOverview;

	private final ToolTableView tools;
	private final ToolTableView preprocessors;
	private final ToolTableView operators;
	private final ToolTableView converters;

	Overview(final ConfigWizard parent) {
		super();

		this.parent = parent;

		final VBox toolBox = new VBox();
		this.tools = new ToolTableView(this, TYPE_TOOL);
		final TitledPane toolPane = new TitledPane("Analysis Tools", this.tools);
		this.preprocessors = new ToolTableView(this, TYPE_PREPROCESSOR);
		final TitledPane preprocessorPane = new TitledPane("Preprocessors", this.preprocessors);
		preprocessorPane.setExpanded(false);
		this.operators = new ToolTableView(this, TYPE_OPERATOR);
		final TitledPane operatorPane = new TitledPane("Operators", this.operators);
		operatorPane.setExpanded(false);
		this.converters = new ToolTableView(this, TYPE_CONVERTER);
		final TitledPane converterPane = new TitledPane("Converters", this.converters);
		converterPane.setExpanded(false);
		toolBox.getChildren().addAll(toolPane, preprocessorPane, operatorPane, converterPane);

		this.editorOverview = new EditorOverview(this);

		this.setCenter(this.editorOverview);
		this.setLeft(toolBox);

		sync();
	}

	public void sync() {
		try {
			if (this.parent.getConfig().getTools() == null || this.parent.getConfig().getTools().getTool() == null) {
				this.tools.sync(null);
			} else {
				this.tools.sync(this.parent.getConfig().getTools().getTool());
			}
			if (this.parent.getConfig().getPreprocessors() == null
					|| this.parent.getConfig().getPreprocessors().getTool() == null) {
				this.preprocessors.sync(null);
			} else {
				this.preprocessors.sync(this.parent.getConfig().getPreprocessors().getTool());
			}
			if (this.parent.getConfig().getOperators() == null
					|| this.parent.getConfig().getOperators().getTool() == null) {
				this.operators.sync(null);
			} else {
				this.operators.sync(this.parent.getConfig().getOperators().getTool());
			}
			if (this.parent.getConfig().getConverters() == null
					|| this.parent.getConfig().getConverters().getTool() == null) {
				this.converters.sync(null);
			} else {
				this.converters.sync(this.parent.getConfig().getConverters().getTool());
			}
		} catch (final Exception e) {
			Log.error("Configuration invalid! Please fix you xml code or create a new configuration.");
		}
	}

	public void edit(final Tool tool, final int type) {
		this.editorOverview.load(tool, type);
	}

	public void apply() {
		sync();
		this.parent.syncEditorXML();
	}

	public ConfigWizard getParentGUI() {
		return this.parent;
	}

	public static String typeToString(int type) {
		if (type == TYPE_TOOL) {
			return "Questions";
		} else if (type == TYPE_PREPROCESSOR) {
			return "Keywords";
		} else if (type == TYPE_OPERATOR) {
			return "Operator";
		} else if (type == TYPE_CONVERTER) {
			return "Analysis Tool";
		} else {
			return "UNKNOWN TYPE";
		}
	}
}