package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ModelLookupTest extends AbstractModelTest {

	@Before
	public void setup() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
	}

	@Test
	public void moduleLookup() throws Throwable {
		Module module = getModel().lookup("my_module1");
		Module expectedModule = getModel().getTopLevel().getModules()
				.get("my_module1");

		assertEquals(expectedModule, module);

		module = getModel().lookup("my_module2");
		expectedModule = getModel().getTopLevel().getModules().get(
				"my_module2");

		assertEquals(expectedModule, module);
	}

	/**
	 * Test that a module that doesn't exist on disk fails on lookup.
	 */
	@Test
	public void moduleLookupError2() throws Throwable {
		assertEquals(null, getModel().lookup("blahkdfkj"));
	}

	/**
	 * Test that a file that exists but isn't a module fails on lookup.
	 */
	@Test
	public void moduleLookupError3() throws Throwable {
		assertEquals(null, getModel().lookup("not_a_module"));
	}

	@Test
	public void submoduleLookup() throws Throwable {
		getModel().loadModule("my_package.my_submodule");
		Module module = getModel().lookup("my_package.my_submodule");
		Module expectedModule = getModel().getTopLevel().getModules()
				.get("my_package").getModules().get("my_submodule");

		assertEquals(expectedModule, module);
	}

	@Test
	public void packageLookup() throws Throwable {
		Module pkg = getModel().lookup("my_package");
		Module expectedPackage = getModel().getTopLevel().getModules()
				.get("my_package");
		assertEquals(expectedPackage, pkg);
	}

	@Test
	public void subpackageLookup() throws Throwable {
		getModel().loadPackage("my_package.my_subpackage");
		Module pkg = getModel().lookup("my_package.my_subpackage");
		Module expectedPackage = getModel().getTopLevel().getModules()
				.get("my_package").getModules().get("my_subpackage");
		assertEquals(expectedPackage, pkg);
	}
}
