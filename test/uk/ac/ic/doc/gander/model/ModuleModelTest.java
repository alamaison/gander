package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test modelling of items declared within a module such as functions and
 * classes.
 */
public class ModuleModelTest extends AbstractModelTest {

	private Module module;

	@Before
	public void setup() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		module = getModel().loadModule("my_module");
	}

	@Test
	public void classes() throws Throwable {
		Map<String, Class> classes = module.getClasses();

		assertKeys(classes, "my_class", "my_class_empty");

		assertEquals("The parent of 'my_class' isn't 'my_module'", module,
				classes.get("my_class").getParentScope());
	}

	@Test
	public void methods() throws Throwable {
		Map<String, Function> methods = module.getClasses().get("my_class")
				.getFunctions();

		assertKeys(methods, "my_method_empty");
	}

	@Test
	public void functions() throws Throwable {
		Map<String, Function> functions = module.getFunctions();

		assertKeys(functions, "my_free_function", "test_nesting",
				"test_nesting_class");
	}

	@Test
	public void nestedFunctions() throws Throwable {
		Map<String, Function> functions = module.getFunctions().get(
				"test_nesting").getFunctions();

		assertKeys(functions, "my_nested_def");
	}

	@Test
	public void nestedClassInFunction() throws Throwable {
		Map<String, Class> classes = module.getFunctions().get(
				"test_nesting_class").getClasses();

		assertKeys(classes, "nested_class");

		Class nested = classes.get("nested_class");
		assertTrue(nested.getFunctions().containsKey("__init__"));
	}

	@Test
	public void nestedClassInClass() throws Throwable {
		Map<String, Class> classes = module.getFunctions().get(
				"test_nesting_class").getClasses().get("nested_class")
				.getClasses();

		assertKeys(classes, "really_nested_class");
	}
}
