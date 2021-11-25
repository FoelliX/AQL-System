package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Intentfilter;

public class ManifestHelperTest {
	public static final File INTENT_MATCHING_3 = new File("examples/scenarios/ImplicitIntentMatching3.apk");

	@Test
	void getDataTest() {
		final ManifestInfo info = ManifestHelper.getInstance().getManifest(INTENT_MATCHING_3);
		final StringBuilder sb = new StringBuilder();
		for (final Intentfilter filter : info.getAllIntentfilters().getIntentfilter()) {
			for (final Data data : filter.getData()) {
				sb.append(Helper.toString(data));
			}
		}
		// SHA-256 of sb.toString()
		assertEquals("9a8b244112f6b81d99fbdcd2a2838eb795da09640141ba2d732d219a279d6f3f",
				HashHelper.sha256Hash(sb.toString()));
	}
}