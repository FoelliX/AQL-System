package de.foellix.aql.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.foellix.aql.config.Tool;

public class DefaultConverterRegistryTest {
	@Test
	public void test01() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid");
		tool.setVersion("1");

		assertEquals("DefaultConverter for FlowDroid", test(tool));
	}

	@Test
	public void test02() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid2");
		tool.setVersion("1");

		assertEquals("DefaultConverter for FlowDroid*", test(tool));
	}

	@Test
	public void test03() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid");
		tool.setVersion("2.7.1");

		assertEquals("DefaultConverter for FlowDroid*", test(tool));
	}

	@Test
	public void test04() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid");
		tool.setVersion("old");

		assertEquals("DefaultConverter for FlowDroid", test(tool));
	}

	@Test
	public void test05() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid271");
		tool.setVersion("1");

		assertEquals("DefaultConverter for FlowDroid*", test(tool));
	}

	@Test
	public void test06() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid271");
		tool.setVersion("271");

		assertEquals("DefaultConverter for FlowDroid*", test(tool));
	}

	@Test
	public void test07() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid");

		assertEquals("DefaultConverter for FlowDroid*", test(tool));
	}

	@Test
	public void test08() {
		final Tool tool = new Tool();
		tool.setName("FlowDroid1");

		assertEquals("DefaultConverter for FlowDroid", test(tool));
	}

	@Test
	public void test09() {
		final Tool tool = new Tool();
		tool.setName("Amandroid312");
		tool.setVersion("1");

		assertEquals("DefaultConverter for Amandroid", test(tool));
	}

	@Test
	public void test10() {
		final Tool tool = new Tool();
		tool.setName("IC3");
		tool.setVersion("1");

		assertEquals("DefaultConverter for IC3", test(tool));
	}

	@Test
	public void test11() {
		final Tool tool = new Tool();
		tool.setName("IccTA");
		tool.setVersion("1");

		assertEquals("DefaultConverter for IccTA", test(tool));
	}

	private String test(Tool tool) {
		boolean noException = true;

		Tool converter = null;
		try {
			converter = DefaultConverterRegistry.getInstance().getConverter(tool);
		} catch (final Exception e) {
			noException = false;
		}

		assertTrue(noException);
		assertNotNull(converter);

		return converter.getName();
	}
}