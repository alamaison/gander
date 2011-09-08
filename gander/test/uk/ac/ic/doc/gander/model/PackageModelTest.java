package uk.ac.ic.doc.gander.model;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test modelling of package module, i.e. __init__.py.
 */
public class PackageModelTest extends AbstractModelTest {

	@Before
	public void setup() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
	}

	@Test
	public void classes() throws Throwable {
		Module pkg = getModel().loadPackage("my_package");
		Map<String, Class> classes = pkg.getClasses();

		assertKeys(classes, "PackageClass");
	}

	@Test
	public void functions() throws Throwable {
		Module pkg = getModel().loadPackage("my_package");
		Map<String, Function> functions = pkg.getFunctions();

		assertKeys(functions, "package_function");
	}

	@Test
	public void submoduleFunctions() throws Throwable {
		Module loadedModule = getModel().loadModule("my_package.my_submodule");
		Map<String, Function> functions = loadedModule.getFunctions();

		assertKeys(functions, "submodule_function");
	}

	@Test
	public void submoduleClasses() throws Throwable {
		Module loadedModule = getModel().loadModule("my_package.my_submodule");
		Map<String, Class> classes = loadedModule.getClasses();

		assertKeys(classes, "SubmoduleClass");
	}
}
