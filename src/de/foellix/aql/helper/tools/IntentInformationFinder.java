package de.foellix.aql.helper.tools;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Intent;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.AsteriskMap;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.ManifestHelper;
import de.foellix.aql.helper.ManifestInfo;
import de.foellix.aql.helper.SootHelper;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public class IntentInformationFinder {
	private static final String NO_APK_MSG = "Cannot run IntentInformationFinder since apk file was not set!";

	private static final String INTENT = "Intent";
	private static final String INTENTFILTER = "Intentfilter";
	private static final String INTENTSINK = "Intentsink";
	private static final String INTENTSOURCE = "Intentsource";

	private static final File INTENT_INFORMATION_FILE = new File("data/IntentInformation.txt");

	private static Map<String, Set<String>> intentInformationMap = null;

	private File apkFile;
	private Collection<SootClass> classes = null;

	private Intents intents = null;
	private Intentfilters intentfilters = null;
	private Intentsinks intentsinks = null;
	private Intentsources intentsources = null;
	private boolean intentInformationRequested = false;
	private boolean intentFiltersRequested = false;

	public IntentInformationFinder(File apkFile) {
		if (intentInformationMap == null) {
			intentInformationMap = new AsteriskMap();
			((AsteriskMap) intentInformationMap).load(INTENT_INFORMATION_FILE);
		}
		this.intents = new Intents();
		this.intentfilters = new Intentfilters();
		this.intentsinks = new Intentsinks();
		this.intentsources = new Intentsources();
		this.intentInformationRequested = false;
		this.intentFiltersRequested = false;

		this.apkFile = apkFile;
		this.classes = SootHelper.getScene(apkFile).getApplicationClasses();
	}

	private void getIntentInformation() {
		if (this.apkFile == null) {
			Log.error(NO_APK_MSG);
			return;
		}
		if (!this.intentInformationRequested) {
			final App app = Helper.createApp(this.apkFile);
			for (final SootClass sc : this.classes) {
				if (sc.isConcrete()) {
					for (final SootMethod sm : sc.getMethods()) {
						if (sm.isConcrete()) {
							final Body b = sm.retrieveActiveBody();
							if (b != null) {
								for (final Unit u : b.getUnits()) {
									if (u instanceof Stmt) {
										final Stmt ss = (Stmt) u;
										if (ss.containsInvokeExpr()) {
											final String invokeStr = ss.getInvokeExpr().toString();
											if (intentInformationMap.get(invokeStr) != null) {
												final Reference ref = new Reference();
												ref.setStatement(Helper.createStatement(u.toString()));
												ref.setMethod(sm.toString());
												ref.setClassname(sc.toString());
												ref.setApp(app);
												if (intentInformationMap.get(invokeStr).contains(INTENT)) {
													final Intent item = new Intent();
													item.setReference(ref);
													this.intents.getIntent().add(item);
												} else if (intentInformationMap.get(invokeStr).contains(INTENTFILTER)) {
													final Intentfilter item = new Intentfilter();
													item.getAction().add("");
													item.getCategory().add("");
													item.getData().add(new Data());
													item.setReference(ref);
													this.intentfilters.getIntentfilter().add(item);
												} else if (intentInformationMap.get(invokeStr).contains(INTENTSINK)) {
													final Intentsink item = new Intentsink();
													item.setReference(ref);
													this.intentsinks.getIntentsink().add(item);
												} else if (intentInformationMap.get(invokeStr).contains(INTENTSOURCE)) {
													final Intentsource item = new Intentsource();
													item.setReference(ref);
													this.intentsources.getIntentsource().add(item);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			this.intentInformationRequested = true;
		}
	}

	public Intents getIntents() {
		getIntentInformation();
		return this.intents;
	}

	public Intentfilters getIntentfilters() {
		getIntentInformation();
		if (!this.intentFiltersRequested) {
			final ManifestInfo manifestInfo = ManifestHelper.getInstance().getManifest(this.apkFile);
			this.intentfilters.getIntentfilter().addAll(manifestInfo.getAllIntentfilters().getIntentfilter());
			this.intentFiltersRequested = true;
		}
		return this.intentfilters;
	}

	public Intentsinks getIntentsinks() {
		getIntentInformation();
		return this.intentsinks;
	}

	public Intentsources getIntentsources() {
		getIntentInformation();
		return this.intentsources;
	}
}