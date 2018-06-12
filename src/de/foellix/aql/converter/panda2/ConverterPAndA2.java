package de.foellix.aql.converter.panda2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterPAndA2 implements IConverter {
	@Override
	public Answer parse(final File resultFile, final ToolTaskInfo taskInfo) {
		final Answer answer = new Answer();
		answer.setPermissions(new Permissions());

		try {
			Reference currentRef = null;
			String statement;
			String method;
			String classname;

			final FileReader fr = new FileReader(resultFile);
			final BufferedReader br = new BufferedReader(fr);

			String zeile = "";
			boolean startReading = false;
			while ((zeile = br.readLine()) != null) {
				if (!startReading) {
					if (zeile.contains("Textual result - Intra-App Permission Usage Analysis")) {
						startReading = true;
					}
					continue;
				}
				if (!zeile.equals("")) {
					if (zeile.startsWith("-----")) {
						if (zeile.contains("<") && zeile.contains(":") && !zeile.contains(":=")
								&& !zeile.contains("return")) {
							statement = Helper.cut(zeile, "-----", " in method ");
							method = Helper.cut(zeile, " in method ", "-----");
							classname = Helper.cut(method, "<", ": ");

							currentRef = new Reference();
							currentRef.setApp(taskInfo.getQuestion().getReferences().get(0).getApp());
							currentRef.setClassname(classname);
							currentRef.setMethod(method);
							try {
								currentRef.setStatement(Helper.fromStatementString(statement));
							} catch (final StringIndexOutOfBoundsException e) {
								// Do nothing to simply ignore this statement,
								// which is not relevant for the outcome.
							}
						}
					} else if (!zeile.startsWith("INFO") && !zeile.startsWith("DEBUG")
							&& !zeile.startsWith("Textual result")) {
						final String permissionStr = Helper.cut(zeile, null, " ");
						final Permission permission = new Permission();
						permission.setName(permissionStr);
						permission.setReference(currentRef);

						final String permissionGroupStr = Helper.cut(zeile, "(", ")");
						final Attributes attributes = new Attributes();
						final Attribute attribute = new Attribute();
						attribute.setName("PermissionGroup");
						attribute.setValue(permissionGroupStr);
						attributes.getAttribute().add(attribute);
						permission.setAttributes(attributes);

						answer.getPermissions().getPermission().add(permission);

						currentRef = Helper.copy(currentRef);
					}
				}
			}

			br.close();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading file: " + resultFile.getAbsolutePath());
			return null;
		}

		return answer;
	}
}
