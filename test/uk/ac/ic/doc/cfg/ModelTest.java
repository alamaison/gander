package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Test;

import uk.ac.ic.doc.cfg.model.Class;
import uk.ac.ic.doc.cfg.model.Function;
import uk.ac.ic.doc.cfg.model.Method;
import uk.ac.ic.doc.cfg.model.Module;
import uk.ac.ic.doc.cfg.model.Package;

public class ModelTest {

	private static final String PACKAGE_STRUCTURE_PROJ = "python_test_code/package_structure";

	private static final String MODULE_STRUCTURE_PROJ = "python_test_code/model_structure";

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	@Test
	public void testGetTopLevelPackages() throws Throwable {
		Model model = createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Package> packages = model.getTopLevelPackage()
				.getPackages();
		assertEquals(1, packages.size());
		assertTrue(packages.containsKey("my_package"));
		assertEquals("my_package", packages.get("my_package").getName());
	}

	@Test
	public void testGetTopLevelModules() throws Throwable {
		Model model = createTestModel(PACKAGE_STRUCTURE_PROJ);
		Map<String, Module> modules = model.getTopLevelPackage().getModules();
		assertEquals(2, modules.size());
		assertTrue(modules.containsKey("my_module1"));
		assertTrue(modules.containsKey("my_module2"));
	}

	@Test
	public void testGetClasses() throws Throwable {
		Model model = createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Class> classes = model.getTopLevelPackage().getPackages()
				.get("my_package").getModules().get("my_module").getClasses();
		assertEquals(2, classes.size());
		assertTrue(classes.containsKey("my_class_empty"));
	}

	@Test
	public void testGetMethods() throws Throwable {
		Model model = createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Method> methods = model.getTopLevelPackage().getPackages()
				.get("my_package").getModules().get("my_module").getClasses()
				.get("my_class").getMethods();
		assertEquals(1, methods.size());
		assertTrue(methods.containsKey("my_method_empty"));
	}

	@Test
	public void testGetFunctions() throws Throwable {
		Model model = createTestModel(MODULE_STRUCTURE_PROJ);
		Map<String, Function> functions = model.getTopLevelPackage().getPackages()
				.get("my_package").getModules().get("my_module").getFunctions();
		assertEquals(1, functions.size());
		assertTrue(functions.containsKey("my_free_function"));
	}

}
