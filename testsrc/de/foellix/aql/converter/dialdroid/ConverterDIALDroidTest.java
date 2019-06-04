package de.foellix.aql.converter.dialdroid;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterDIALDroidTest {
	// FIXME: Add proper (complete) example. Until then test is deactivated.

	// @Test
	public void test() {
		QuestionPart qp = new QuestionPart();
		App app = new App();
		app.setFile("Test");
		Reference ref = new Reference();
		ref.setApp(app);
		qp.addReference(ref);
		final ToolTaskInfo taskInfo = new ToolTaskInfo(null, qp);

		boolean noException = true;
		try {
			// Test DIALDroid SIM&SMS App
			qp = new QuestionPart();
			app = new App();
			Hashes hashes = new Hashes();
			Hash hash = new Hash();
			hash.setType(KeywordsAndConstants.HASH_TYPE_SHA256);
			hash.setValue("058962877C7783C6F3760DFE31AEE356E22B140E704099F24E3A8058CF9C01D0");
			hashes.getHash().add(hash);
			app.setHashes(hashes);
			app.setFile("/some/path/SIMApp.apk");
			ref = new Reference();
			ref.setApp(app);
			qp.addReference(ref);
			app = new App();
			hashes = new Hashes();
			hash = new Hash();
			hash.setType(KeywordsAndConstants.HASH_TYPE_SHA256);
			hash.setValue("644D6F1E0C18F39121B8D8CD986D91EC8EEA75B56940385A1540B90609105824");
			hashes.getHash().add(hash);
			app.setHashes(hashes);
			app.setFile("/some/path/SMSApp.apk");
			ref = new Reference();
			ref.setApp(app);
			qp.addReference(ref);

			final IConverter compiler1 = new ConverterDIALDroid();
			final Answer answer1 = compiler1.parse(null, taskInfo);
			System.out.println(Helper.toString(answer1));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
