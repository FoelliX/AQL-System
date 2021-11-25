package de.foellix.aql.helper.tools;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Sink;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Source;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.helper.AsteriskMap;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.helper.SootHelper;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public class SourceSinkFinder {
	private static final String NO_APK_MSG = "Cannot run SourceSinkFinder since apk file was not set!";
	private static final File SOURCES_AND_SINKS_FILE = new File("data/SourcesAndSinks.txt");

	private static String lastLoadedSaSFileHash;
	private static Map<String, Set<String>> sourceSinkMap = null;

	private File apkFile;
	private Collection<SootClass> classes = null;

	private Sources sources = null;
	private Sinks sinks = null;
	private boolean sourcesAndSinksRequested = false;

	public SourceSinkFinder(File apkFile) {
		this(apkFile, SOURCES_AND_SINKS_FILE);
	}

	public SourceSinkFinder(File apkFile, File sourceAndSinkFile) {
		final String hash = HashHelper.sha256Hash(sourceAndSinkFile);
		if (lastLoadedSaSFileHash == null || !lastLoadedSaSFileHash.equals(hash)) {
			lastLoadedSaSFileHash = hash;
			sourceSinkMap = new AsteriskMap();
			((AsteriskMap) sourceSinkMap).load(sourceAndSinkFile);
		}

		this.sources = new Sources();
		this.sinks = new Sinks();
		this.sourcesAndSinksRequested = false;

		this.apkFile = apkFile;
		this.classes = SootHelper.getScene(apkFile).getApplicationClasses();
	}

	private void getSourcesAndSinks() {
		if (this.apkFile == null) {
			Log.error(NO_APK_MSG);
			return;
		}
		if (!this.sourcesAndSinksRequested) {
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
											Reference ref = null;
											// In class
											String invokeStr = ss.getInvokeExpr().toString();
											if (sourceSinkMap.get(invokeStr) != null) {
												ref = new Reference();
												ref.setStatement(Helper.createStatement(u.toString()));
												ref.setMethod(sm.toString());
												ref.setClassname(sc.toString());
												ref.setApp(app);
												if (sourceSinkMap.get(invokeStr)
														.contains(KeywordsAndConstantsHelper.SOURCE)) {
													Source item = new Source();
													item.setReference(ref);
													this.sources.getSource().add(item);
													final Reference altRef = getAlternativeReference(ss, ref);
													if (altRef != null) {
														item = new Source();
														item.setReference(altRef);
														this.sources.getSource().add(item);
													}
												} else if (sourceSinkMap.get(invokeStr)
														.contains(KeywordsAndConstantsHelper.SINK)) {
													Sink item = new Sink();
													item.setReference(ref);
													this.sinks.getSink().add(item);
													final Reference altRef = getAlternativeReference(ss, ref);
													if (altRef != null) {
														item = new Sink();
														item.setReference(altRef);
														this.sinks.getSink().add(item);
													}
												}
											}

											// Not in class
											invokeStr = getAlternativeInvokeStr(ss);
											if (invokeStr != null && sourceSinkMap.get(invokeStr) != null) {
												if (ref == null) {
													ref = new Reference();
													ref.setStatement(Helper.createStatement(u.toString()));
													ref.setMethod(sm.toString());
													ref.setClassname(sc.toString());
													ref.setApp(app);
													if (sourceSinkMap.get(invokeStr)
															.contains(KeywordsAndConstantsHelper.SOURCE)) {
														final Source item = new Source();
														item.setReference(ref);
														this.sources.getSource().add(item);
													} else if (sourceSinkMap.get(invokeStr)
															.contains(KeywordsAndConstantsHelper.SINK)) {
														final Sink item = new Sink();
														item.setReference(ref);
														this.sinks.getSink().add(item);
													}
												}
												ref = getAlternativeReference(ss, ref);
												if (ref != null) {
													if (sourceSinkMap.get(invokeStr)
															.contains(KeywordsAndConstantsHelper.SOURCE)) {
														final Source item = new Source();
														item.setReference(ref);
														this.sources.getSource().add(item);
													} else if (sourceSinkMap.get(invokeStr)
															.contains(KeywordsAndConstantsHelper.SINK)) {
														final Sink item = new Sink();
														item.setReference(ref);
														this.sinks.getSink().add(item);
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
			}
			this.sourcesAndSinksRequested = true;
		}
	}

	private String getAlternativeInvokeStr(Stmt ss) {
		if (ss.containsInvokeExpr()) {
			final String newClass = ss.getInvokeExpr().getMethod().getDeclaringClass().toString();
			String oldClass = ss.getInvokeExpr().toString();
			try {
				oldClass = oldClass.substring(oldClass.indexOf('<') + 1, oldClass.indexOf(':'));
			} catch (final IndexOutOfBoundsException e) {
				return null;
			}
			if (!newClass.equals(oldClass)) {
				return ss.toString().replaceFirst(oldClass, newClass);
			}
		}
		return null;
	}

	private Reference getAlternativeReference(Stmt ss, Reference ref) {
		if (ss.containsInvokeExpr()) {
			final String newClass = ss.getInvokeExpr().getMethod().getDeclaringClass().toString();
			String oldClass = ref.getStatement().getStatementgeneric();
			try {
				oldClass = oldClass.substring(0, oldClass.indexOf(':'));
			} catch (final IndexOutOfBoundsException e) {
				return null;
			}
			if (!newClass.equals(oldClass)) {
				final Reference newRef = Helper.copy(ref);
				newRef.setStatement(
						Helper.createStatement(ref.getStatement().getStatementfull().replaceFirst(oldClass, newClass)));
				return newRef;
			}
		}
		return null;
	}

	public Sources getSources() {
		getSourcesAndSinks();
		return this.sources;
	}

	public Sinks getSinks() {
		getSourcesAndSinks();
		return this.sinks;
	}
}