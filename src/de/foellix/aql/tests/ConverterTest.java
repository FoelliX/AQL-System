package de.foellix.aql.tests;

import java.io.File;

import de.foellix.aql.converter.IConverter;
import de.foellix.aql.converter.amandroid.ConverterAmandroid;
import de.foellix.aql.converter.dialdroid.ConverterDIALDroid;
import de.foellix.aql.converter.didfail.ConverterDidFail;
import de.foellix.aql.converter.droidsafe.ConverterDroidSafe;
import de.foellix.aql.converter.flowdroid.ConverterFD;
import de.foellix.aql.converter.ic3.ConverterIC3;
import de.foellix.aql.converter.panda2.ConverterPAndA2;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterTest {
	public static void main(final String args[]) {
		// Run tests
		final boolean testIc3 = false;
		final boolean testFd = false;
		final boolean testIccta = false;
		final boolean testPAndA2 = false;
		final boolean testDidFail = false;
		final boolean testAmandroid = false;
		final boolean testDIALDroid = false;
		final boolean testDroidSafe = true;

		QuestionPart qp = new QuestionPart();
		App app = new App();
		app.setFile("Test");
		Reference ref = new Reference();
		ref.setApp(app);
		qp.addReference(ref);
		final ToolTaskInfo taskInfo = new ToolTaskInfo(null, qp);

		try {
			if (testIc3) {
				// Test IC3 SIM App
				final File appfile1 = new File("examples/scenario1/SIMApp.dat");
				final IConverter compiler1 = new ConverterIC3();
				final Answer answer1 = compiler1.parse(appfile1, taskInfo);
				System.out.println(Helper.toString(answer1));

				// Test IC3 SMS App
				final File appfile2 = new File("examples/scenario1/SMSApp.dat");
				final IConverter compiler2 = new ConverterIC3();
				final Answer answer2 = compiler2.parse(appfile2, taskInfo);
				System.out.println(Helper.toString(answer2));
			}

			if (testFd) {
				// Test FD SIM App
				final File appfile1 = new File("examples/scenario1/SIMApp_result.txt");
				final IConverter compiler1 = new ConverterFD();
				final Answer answer1 = compiler1.parse(appfile1, taskInfo);
				System.out.println(Helper.toString(answer1));

				// Test FD SMS App
				final File appfile2 = new File("examples/scenario1/SMSApp_result.txt");
				final IConverter compiler2 = new ConverterFD();
				final Answer answer2 = compiler2.parse(appfile2, taskInfo);
				System.out.println(Helper.toString(answer2));
			}

			if (testIccta) {
				// Test IccTa SIM + SMS App
				// FIXME: File missing
				final File appfile1 = new File("examples/forConverterConst.txt");
				final IConverter compiler1 = new ConverterFD();
				final Answer answer1 = compiler1.parse(appfile1, taskInfo);
				System.out.println(Helper.toString(answer1));
			}

			if (testPAndA2) {
				// Test Panda SIM App
				final File appfile1 = new File("examples/scenario1/SIMApp_panda2_result.txt");
				final IConverter compiler1 = new ConverterPAndA2();
				final Answer answer1 = compiler1.parse(appfile1, taskInfo);
				System.out.println(Helper.toString(answer1));

				// Test Panda SMS App
				final File appfile2 = new File("examples/scenario1/SMSApp_panda2_result.txt");
				final IConverter compiler2 = new ConverterPAndA2();
				final Answer answer2 = compiler2.parse(appfile2, taskInfo);
				System.out.println(Helper.toString(answer2));
			}

			if (testDidFail) {
				// Test DidFail SIM&SMS App
				qp = new QuestionPart();
				app = new App();
				app.setFile("/some/path/SIMApp.apk");
				ref = new Reference();
				ref.setApp(app);
				qp.addReference(ref);
				app = new App();
				app.setFile("/some/path/SMSApp.apk");
				ref = new Reference();
				ref.setApp(app);
				qp.addReference(ref);

				final File appfile1 = new File("examples/scenario1/didfail2");
				final IConverter compiler1 = new ConverterDidFail();
				// final Answer answer1 = compiler1.parse(appfile1, qp);
				final Answer answer1 = compiler1.parse(appfile1, null);
				System.out.println(Helper.toString(answer1));
			}

			if (testAmandroid) {
				// Test Amandroid DroidBench ActivityCommunication2 App
				qp = new QuestionPart();
				app = Helper.createApp("/some/path/ActivityCommunication2.apk");
				ref = new Reference();
				ref.setApp(app);
				qp.addReference(ref);

				final File appfile1 = new File("examples/ActivityCommunication2/amandroid2/result/AppData.txt");
				final IConverter compiler1 = new ConverterAmandroid();
				final Answer answer1 = compiler1.parse(appfile1, taskInfo);
				System.out.println(Helper.toString(answer1));
			}

			if (testDIALDroid) {
				// Test DIALDroid SIM&SMS App
				// FIXME: Add proper (complete) example
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
			}

			if (testDroidSafe) {
				// Test DroidSafe SIM + SMS App
				final File appfile1 = new File("examples/scenario1/droidsafe");
				final IConverter compiler1 = new ConverterDroidSafe();
				final Answer answer1 = compiler1.parse(appfile1, taskInfo);
				System.out.println(Helper.toString(answer1));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
