package de.foellix.aql.helper;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Reference;

public class ConnectHelper {
	public static void computeTransitiveHull(Flows flows, EqualsOptions equalsOptions) {
		boolean fixpoint = false;
		while (!fixpoint) {
			fixpoint = true;
			final List<Flow> addPaths = new ArrayList<>();
			for (final Flow path1 : flows.getFlow()) {
				for (final Flow path2 : flows.getFlow()) {
					if (path1 != path2) {
						Reference path1From = null;
						Reference path1To = null;
						Reference path2From = null;
						Reference path2To = null;
						for (final Reference refPath1 : path1.getReference()) {
							if (refPath1.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM)) {
								path1From = refPath1;
							} else if (refPath1.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO)) {
								path1To = refPath1;
							}
						}
						for (final Reference refPath2 : path2.getReference()) {
							if (refPath2.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM)) {
								path2From = refPath2;
							} else if (refPath2.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO)) {
								path2To = refPath2;
							}
						}

						if (path1From != null && path1To != null && path2From != null && path2To != null) {
							if (EqualsHelper.equals(path1To, path2From, equalsOptions)) {
								boolean exists = false;

								for (final Flow checkPath : flows.getFlow()) {
									Reference checkFrom = null;
									Reference checkTo = null;
									for (final Reference checkRef : checkPath.getReference()) {
										if (checkRef.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM)) {
											checkFrom = checkRef;
										} else if (checkRef.getType()
												.equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO)) {
											checkTo = checkRef;
										}
									}

									if (checkFrom != null && checkTo != null) {
										if (EqualsHelper.equals(path1From, checkFrom, equalsOptions)
												&& EqualsHelper.equals(path2To, checkTo, equalsOptions)) {
											exists = true;
										}
									} else {
										exists = true;
									}
								}

								if (!exists) {
									fixpoint = false;
									addPaths.add(connect(path1From, path2To));
								}
							}
						}
					}
				}
			}
			flows.getFlow().addAll(addPaths);
		}
	}

	public static Flow connect(final Reference from, final Reference to) {
		final Flow newPath = new Flow();

		final Reference newFrom = new Reference();
		newFrom.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
		newFrom.setApp(from.getApp());
		newFrom.setClassname(from.getClassname());
		newFrom.setMethod(from.getMethod());
		newFrom.setStatement(from.getStatement());

		final Reference newTo = new Reference();
		newTo.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
		newTo.setApp(to.getApp());
		newTo.setClassname(to.getClassname());
		newTo.setMethod(to.getMethod());
		newTo.setStatement(to.getStatement());

		newPath.getReference().add(newFrom);
		newPath.getReference().add(newTo);

		return newPath;
	}

	public static Flow connect(final Intentsink from, final Intentsource to) {
		final Flow newPath = new Flow();

		final Reference newFrom = new Reference();
		newFrom.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
		newFrom.setApp(from.getReference().getApp());
		newFrom.setClassname(from.getReference().getClassname());
		newFrom.setMethod(from.getReference().getMethod());
		newFrom.setStatement(from.getReference().getStatement());

		final Reference newTo = new Reference();
		newTo.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
		newTo.setApp(to.getReference().getApp());
		newTo.setClassname(to.getReference().getClassname());
		newTo.setMethod(to.getReference().getMethod());
		newTo.setStatement(to.getReference().getStatement());

		newPath.getReference().add(newFrom);
		newPath.getReference().add(newTo);

		return newPath;
	}
}