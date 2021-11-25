package de.foellix.aql.ui.gui;

import de.foellix.aql.system.AQLSystem;
import javafx.stage.Stage;

public interface IGUI {
	public Stage getStage();

	public void newFile();

	public void open();

	public void save();

	public void saveAs();

	public void exit();

	public AQLSystem getSystem();
}
