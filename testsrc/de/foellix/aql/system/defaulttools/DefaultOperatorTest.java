package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.defaulttools.operators.DefaultIntersectOperator;
import de.foellix.aql.system.defaulttools.operators.DefaultMinusOperator;
import de.foellix.aql.system.defaulttools.operators.DefaultUnifyOperator;

public class DefaultOperatorTest {
	@Test
	public void test() {
		boolean noException = true;
		try {
			// Prepare reference
			final Statement statement = new Statement();
			statement.setStatementfull("Test");
			statement.setStatementgeneric("Test");
			final App app = Helper.createApp(new File("examples/scenarios/DirectLeak1.apk"));
			final Reference ref = new Reference();
			ref.setStatement(statement);
			ref.setMethod("Test");
			ref.setClassname("Test");
			ref.setApp(app);

			// Prepare answers
			final Answer a1 = new Answer();
			a1.setPermissions(new Permissions());
			final Permission item1 = new Permission();
			item1.setName("TEST_PERMISSION_1");
			item1.setReference(ref);
			a1.getPermissions().getPermission().add(item1);
			final Permission item2 = new Permission();
			item2.setName("TEST_PERMISSION_2");
			item2.setReference(ref);
			a1.getPermissions().getPermission().add(item2);

			final Answer a2 = new Answer();
			a2.setPermissions(new Permissions());
			final Permission item3 = new Permission();
			item3.setName("TEST_PERMISSION_1");
			item3.setReference(ref);
			a2.getPermissions().getPermission().add(item3);
			final Permission item4 = new Permission();
			item4.setName("TEST_PERMISSION_2");
			item4.setReference(ref);
			a2.getPermissions().getPermission().add(item4);

			final Answer a3 = new Answer();
			a3.setPermissions(new Permissions());
			final Permission item5 = new Permission();
			item5.setName("TEST_PERMISSION_3");
			item5.setReference(ref);
			a3.getPermissions().getPermission().add(item5);

			final Answer a4 = new Answer();

			// Initialize default operators
			final DefaultMinusOperator minus = new DefaultMinusOperator();
			final DefaultIntersectOperator intersect = new DefaultIntersectOperator();
			final DefaultUnifyOperator unify = new DefaultUnifyOperator();

			// Positive asserts
			assertTrue(EqualsHelper.equals(a1, a2));
			assertTrue(EqualsHelper.equals(minus.minus(a1, a2), a4));
			assertTrue(EqualsHelper.equals(intersect.intersect(a1, a2), a1));
			assertTrue(EqualsHelper.equals(unify.unify(a1, a3), unify.unify(a2, a3)));
			assertTrue(EqualsHelper.equals(minus.minus(unify.unify(a1, a3), a2), a3));
			assertTrue(EqualsHelper.equals(intersect.intersect(unify.unify(a1, a3), a2), a1));

			// Negative asserts
			assertTrue(!EqualsHelper.equals(a1, a3));
			assertTrue(!EqualsHelper.equals(minus.minus(a1, a3), a4));
			assertTrue(!EqualsHelper.equals(intersect.intersect(a1, a3), a1));
			assertTrue(!EqualsHelper.equals(unify.unify(a1, a2), unify.unify(a2, a3)));
			assertTrue(!EqualsHelper.equals(minus.minus(unify.unify(a1, a3), a3), a3));
			assertTrue(!EqualsHelper.equals(intersect.intersect(unify.unify(a1, a3), a3), a1));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}