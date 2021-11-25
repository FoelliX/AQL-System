package de.foellix.aql.transformations;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.foellix.aql.Log;

public class RulesHandler {
	public static final File DEFAULT_RULES_FILE = new File("rules.xml");

	private List<File> rulesFiles;
	private Rules rules;

	private static RulesHandler instance = new RulesHandler();

	private RulesHandler() {
		setRulesFile(DEFAULT_RULES_FILE);
	}

	public static RulesHandler getInstance() {
		return instance;
	}

	public void setRulesFiles(List<File> rulesFiles) {
		this.rulesFiles = rulesFiles;
		init();
	}

	public void setRulesFile(File rulesFile) {
		if (this.rulesFiles == null) {
			this.rulesFiles = new ArrayList<>();
		} else {
			this.rulesFiles.clear();
		}
		this.rulesFiles.add(rulesFile);
		init();
	}

	private void init() {
		this.rules = null;
		for (final File rulesFile : this.rulesFiles) {
			if (rulesFile.exists()) {
				try {
					final Reader reader = new FileReader(rulesFile);
					final JAXBContext jaxbContext = JAXBContext.newInstance(Rules.class);
					final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					final Rules rulesTemp = (Rules) jaxbUnmarshaller.unmarshal(reader);
					if (this.rules == null) {
						this.rules = rulesTemp;
					} else {
						this.rules.getRule().addAll(rulesTemp.getRule());
					}
					reader.close();
				} catch (final JAXBException | IOException e) {
					Log.error("Cannot parse rules (XML-)file. It must be corrupted!" + Log.getExceptionAppendix(e));
				}
			} else if (this.rules == null) {
				this.rules = new Rules();
			}
		}
	}

	public List<Rule> getRules() {
		return this.rules.getRule();
	}

	public List<File> getRulesFiles() {
		return this.rulesFiles;
	}
}