package de.foellix.aql.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.datastructure.query.QuestionString;

public class DirectoryReferenceTest {
	@BeforeAll
	public static void setup() {
		// Log.setLogLevel(Log.DEBUG_DETAILED);
		// Log.setShorten(true);
	}

	@Test
	public void test01() {
		final String directoryStr = "examples/scenarios/simsms";

		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString(directoryStr));

		final DefaultQuestion q = new DefaultQuestion();
		q.setEndingSymbol(Question.ENDING_SYMBOL_AQL);
		q.setSubjectOfInterest(Question.QUESTION_TYPE_FLOWS);
		q.setIn(ref);

		assertEquals(getExpected(directoryStr, 0), getActual(resolve(q)));
	}

	@Test
	public void test02() {
		final String directoryStr = "examples/scenarios/simsms";

		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString(directoryStr));

		final DefaultQuestion q = new DefaultQuestion();
		q.setEndingSymbol(Question.ENDING_SYMBOL_AQL);
		q.setSubjectOfInterest(Question.QUESTION_TYPE_FLOWS);
		q.setFrom(ref);

		assertEquals(getExpected(directoryStr, 1), getActual(resolve(q)));
	}

	@Test
	public void test03() {
		final String directoryStr = "examples/scenarios/simsms";

		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString(directoryStr));

		final DefaultQuestion q = new DefaultQuestion();
		q.setEndingSymbol(Question.ENDING_SYMBOL_AQL);
		q.setSubjectOfInterest(Question.QUESTION_TYPE_FLOWS);
		q.setTo(ref);

		assertEquals(getExpected(directoryStr, 2), getActual(resolve(q)));
	}

	@Test
	public void test04() {
		final String directoryStr = "examples/scenarios/simsms";

		final QuestionReference from = new QuestionReference();
		from.setApp(new QuestionString(directoryStr));

		final QuestionReference to = new QuestionReference();
		to.setApp(new QuestionString(directoryStr));

		final DefaultQuestion q = new DefaultQuestion();
		q.setEndingSymbol(Question.ENDING_SYMBOL_AQL);
		q.setSubjectOfInterest(Question.QUESTION_TYPE_FLOWS);
		q.setFrom(from);
		q.setTo(to);

		assertEquals(getExpected(directoryStr, 3), getActual(resolve(q)));
	}

	private List<Question> resolve(Question q) {
		List<Question> qs = new ArrayList<>();
		qs.add(q);

		Log.msg("IN:", Log.DEBUG_DETAILED);
		for (final Question qt : qs) {
			Log.msg(qt.toString(), Log.DEBUG_DETAILED);
		}
		if (Log.logIt(Log.DEBUG_DETAILED)) {
			Log.emptyLine();
		}

		qs = DirectoryResolver.resolveDirectoryReference(qs);

		Log.msg("OUT:", Log.DEBUG_DETAILED);
		final StringBuilder actual = new StringBuilder();
		for (final Question qt : qs) {
			Log.msg(qt.toString(), Log.DEBUG_DETAILED);
			actual.append(qt.toString());
		}
		if (Log.logIt(Log.DEBUG_DETAILED)) {
			Log.emptyLine();
		}

		return qs;
	}

	private String getActual(List<Question> qs) {
		final StringBuilder actual = new StringBuilder();
		for (final Question qt : qs) {
			actual.append(qt.toString() + "\n");
		}
		return actual.toString();
	}

	private String getExpected(String directoryStr, int mode) {
		// Expected
		final File directory = new File(directoryStr);
		final StringBuilder expected = new StringBuilder();

		final File[] apks = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".apk")) {
					return true;
				} else {
					return false;
				}
			}
		});

		final Set<File> done = new HashSet<>();
		for (final File apk1 : apks) {
			switch (mode) {
			case 1: // 1: FROM
				expected.append("Flows FROM App('" + apk1.getAbsolutePath() + "') ?\n");
				break;
			case 2: // 2: TO
				expected.append("Flows TO App('" + apk1.getAbsolutePath() + "') ?\n");
				break;
			case 3: // 3: FROM-TO
				for (final File apk2 : apks) {
					System.out.println(done);
					if (!done.contains(apk2) && apk1 != apk2) {
						done.add(apk1);
						expected.append("Flows FROM App('" + apk1.getAbsolutePath() + "') TO App('"
								+ apk2.getAbsolutePath() + "') ?\n");
						expected.append("Flows FROM App('" + apk2.getAbsolutePath() + "') TO App('"
								+ apk1.getAbsolutePath() + "') ?\n");
					}
				}
				break;
			default: // 0: IN
				expected.append("Flows IN App('" + apk1.getAbsolutePath() + "') ?\n");
				break;
			}
		}
		return expected.toString();
	}
}