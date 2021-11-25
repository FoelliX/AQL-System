package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Target;

public class EqualsHelperTest {
	@Test
	void cutFromFirstToLastTest() {
		final EqualsOptions strict = EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_TARGET, true)
				.setOption(EqualsOptions.PRECISELY_REFERENCE, true);

		final Intentsource is = new Intentsource();

		final Target target = new Target();
		target.getAction().add("de.foellix.aql.aqlbench.LEAK");
		target.getCategory().add("android.intent.category.DEFAULT");
		final Data data = new Data();
		data.setHost("foellix.de");
		data.setPort("80");
		data.setScheme("http");
		target.getData().add(data);
		is.setTarget(target);

		assertTrue(EqualsHelper.equals(is, is));
		assertTrue(EqualsHelper.equals(is, is, strict));
	}
}