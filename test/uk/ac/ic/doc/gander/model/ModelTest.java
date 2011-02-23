package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class ModelTest extends AbstractModelTest {

	@Test
	public void classes() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Class> classes = getModel().getTopLevelPackage().getModules()
				.get("my_module").getClasses();

		assertKeys(classes, "my_class", "my_class_empty");
	}

	@Test
	public void methods() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Function> methods = getModel().getTopLevelPackage().getModules()
				.get("my_module").getClasses().get("my_class").getFunctions();

		assertKeys(methods, "my_method_empty");
	}

	@Test
	public void functions() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Function> functions = getModel().getTopLevelPackage()
				.getModules().get("my_module").getFunctions();

		assertKeys(functions, "my_free_function", "test_nesting",
				"test_nesting_class");
	}

	@Test
	public void noPackages() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		assertTrue(getModel().getTopLevelPackage().getPackages().isEmpty());
	}

	@Test
	public void nestedFunctions() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Function> functions = getModel().getTopLevelPackage()
				.getModules().get("my_module").getFunctions().get(
						"test_nesting").getFunctions();

		assertKeys(functions, "my_nested_def");
	}

	@Test
	public void nestedClassInFunction() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Class> classes = getModel().getTopLevelPackage().getModules()
				.get("my_module").getFunctions().get("test_nesting_class")
				.getClasses();

		assertKeys(classes, "nested_class");

		Class nested = classes.get("nested_class");
		assertTrue(nested.getFunctions().containsKey("__init__"));
	}

	@Test
	public void nestedClassInClass() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Class> classes = getModel().getTopLevelPackage().getModules()
				.get("my_module").getFunctions().get("test_nesting_class")
				.getClasses().get("nested_class").getClasses();

		assertKeys(classes, "really_nested_class");
	}

	@Test
	public void topLevelPackages() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Package> packages = getModel().getTopLevelPackage()
				.getPackages();

		assertKeys(packages, "my_package");
		assertEquals("my_package", packages.get("my_package").getName());
	}

	@Test
	public void topLevelModules() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Module> modules = getModel().getTopLevelPackage().getModules();

		assertKeys(modules, "my_module1", "my_module2");
	}

	@Test
	public void packages() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Package> packages = getModel().getTopLevelPackage()
				.getPackages();

		assertKeys(packages, "my_package");
	}

	@Test
	public void packageModules() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Module> modules = getModel().getTopLevelPackage().getPackages()
				.get("my_package").getModules();

		assertKeys(modules, "my_submodule");
	}

	@Test
	public void packageClasses() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Class> classes = getModel().getTopLevelPackage().getPackages()
				.get("my_package").getClasses();

		assertKeys(classes, "PackageClass");
	}

	@Test
	public void packageFunctions() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Function> functions = getModel().getTopLevelPackage()
				.getPackages().get("my_package").getFunctions();

		assertKeys(functions, "package_function");
	}

	@Test
	public void packageModuleFunctions() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Function> functions = getModel().getTopLevelPackage()
				.getPackages().get("my_package").getModules().get(
						"my_submodule").getFunctions();

		assertKeys(functions, "submodule_function");
	}

	@Test
	public void packageModuleClasses() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Class> functions = getModel().getTopLevelPackage().getPackages()
				.get("my_package").getModules().get("my_submodule")
				.getClasses();

		assertKeys(functions, "SubmoduleClass");
	}
}
