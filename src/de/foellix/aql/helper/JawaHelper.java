package de.foellix.aql.helper;

import de.foellix.aql.datastructure.Parameter;
import de.foellix.aql.datastructure.Statement;

public class JawaHelper {
	public static String toJawa(Statement statement) {
		if (statement.getStatementgeneric() == null || statement.getStatementgeneric().isBlank()) {
			statement.setStatementgeneric(Helper.getStatementgenericSafe(statement));
		}

		final String className = statement.getStatementgeneric()
				.substring((statement.getStatementgeneric().startsWith("<") ? 1 : 0),
						statement.getStatementgeneric().indexOf(':'))
				.replace('.', '/');
		String methodName = Helper.cut(statement.getStatementgeneric(), " ", 2);
		methodName = methodName.substring(0, methodName.indexOf('('));
		final StringBuilder parameters = new StringBuilder();
		if (statement.getParameters() != null && !statement.getParameters().getParameter().isEmpty()) {
			for (final Parameter param : statement.getParameters().getParameter()) {
				parameters.append(toJawaType(param.getType()));
			}
		}
		final String returnType = toJawaType(Helper.cut(statement.getStatementgeneric(), ": ", " ").replace('.', '/'));

		return "L" + className + ";." + methodName + ":(" + parameters.toString() + ")" + returnType;
	}

	public static String toJawaType(String javaTypeStr) {
		if (javaTypeStr == null || javaTypeStr.isEmpty()) {
			return javaTypeStr;
		}
		String array = "";
		if (javaTypeStr.endsWith("[]")) {
			array = "[";
			javaTypeStr = javaTypeStr.replace("[]", "");
		}
		javaTypeStr = javaTypeStr.replace('.', '/');
		if (javaTypeStr.equals("boolean")) {
			return array + "Z";
		} else if (javaTypeStr.equals("short")) {
			return array + "S";
		} else if (javaTypeStr.equals("int")) {
			return array + "I";
		} else if (javaTypeStr.equals("long")) {
			return array + "L";
		} else if (javaTypeStr.equals("float")) {
			return array + "F";
		} else if (javaTypeStr.equals("double")) {
			return array + "D";
		} else if (javaTypeStr.equals("byte")) {
			return array + "B";
		} else if (javaTypeStr.equals("char")) {
			return array + "C";
		} else if (javaTypeStr.equals("void")) {
			return array + "V";
		} else {
			return array + "L" + javaTypeStr + ";";
		}
	}

	public static String toJavaType(String jawaTypeStr) {
		if (jawaTypeStr == null || jawaTypeStr.isEmpty()) {
			return jawaTypeStr;
		}
		if (!jawaTypeStr.contains("/") && jawaTypeStr.replace("[", "").length() > 1) {
			final StringBuilder sb = new StringBuilder();
			boolean array = false;
			for (int i = 0; i < jawaTypeStr.length(); i++) {
				if (jawaTypeStr.charAt(i) == '[') {
					array = true;
				} else {
					if (sb.length() > 0) {
						sb.append(",");
					}
					if (array) {
						sb.append(toJavaType('[' + String.valueOf(jawaTypeStr.charAt(i))));
					} else {
						sb.append(toJavaType(String.valueOf(jawaTypeStr.charAt(i))));
					}
					array = false;
				}
			}
			return sb.toString();
		}
		if (jawaTypeStr.equals("Z")) {
			return "boolean";
		} else if (jawaTypeStr.equals("S")) {
			return "short";
		} else if (jawaTypeStr.equals("I")) {
			return "int";
		} else if (jawaTypeStr.equals("L")) {
			return "long";
		} else if (jawaTypeStr.equals("F")) {
			return "float";
		} else if (jawaTypeStr.equals("D")) {
			return "double";
		} else if (jawaTypeStr.equals("B")) {
			return "byte";
		} else if (jawaTypeStr.equals("C")) {
			return "char";
		} else if (jawaTypeStr.equals("V")) {
			return "void";
		} else {
			if (jawaTypeStr.endsWith(";")) {
				jawaTypeStr = jawaTypeStr.substring(0, jawaTypeStr.length() - 1);
			}
			if (jawaTypeStr.startsWith("[")) {
				return toJavaType(jawaTypeStr.substring(1)) + "[]";
			} else {
				jawaTypeStr = jawaTypeStr.replace("/", ".");
				return jawaTypeStr.substring(1);
			}
		}
	}
}
