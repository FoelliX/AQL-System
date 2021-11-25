package de.foellix.aql.helper.tools;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.foellix.aql.helper.AsteriskMap;
import de.foellix.aql.helper.SootHelper;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public class FeatureFinder {
	private static final File FEATURES_FILE = new File("data/Features.txt");

	private static Map<String, Set<String>> featureMap = null;

	public FeatureFinder() {
		if (featureMap == null) {
			featureMap = new AsteriskMap();
			((AsteriskMap) featureMap).load(FEATURES_FILE);
		}
	}

	public List<String> getFeatures(File apkFile) {
		final Set<String> features = new HashSet<>();

		final Collection<SootClass> classes = SootHelper.getScene(apkFile).getApplicationClasses();
		for (final SootClass sc : classes) {
			if (sc.isConcrete()) {
				for (final SootMethod sm : sc.getMethods()) {
					addFeatures(features, sm);
				}
			}
		}

		// Sort and return
		final List<String> sortedFeatures = new LinkedList<>(features);
		Collections.sort(sortedFeatures);
		return sortedFeatures;
	}

	public void addFeatures(Collection<String> features, SootMethod sm) {
		if (sm.isConcrete()) {
			final Body b = sm.retrieveActiveBody();
			if (b != null) {
				for (final Unit u : b.getUnits()) {
					if (u instanceof Stmt) {
						final Stmt ss = (Stmt) u;
						if (ss.containsInvokeExpr()) {
							final String invokeStr = ss.getInvokeExpr().toString();
							if (featureMap.get(invokeStr) != null) {
								features.addAll(featureMap.get(invokeStr));
							}
						}
					}
				}
			}
		}
	}
}