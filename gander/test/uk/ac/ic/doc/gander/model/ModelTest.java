package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

/**
 * Test loading modules and packages that don't import anything else.
 */
public class ModelTest extends AbstractModelTest {

	@Before
	public void setup() throws Throwable {
		createTestModel(PACKAGE_STRUCTURE_PROJ);
	}

	/**
	 * The model simulates the runtime behaviour of the Python interpreter.
	 * Before any modules are loaded only the top-level package should exist
	 * and, shouldn't contain any modules or packages. These only appear once
	 * they've been loaded.
	 */
	@Test
	public void onlyTopLevelKnownBeforeLoading() throws Throwable {

		assertTrue("Top-level package must always exist", getModel()
				.getTopLevel() != null);
		assertEquals("Modules shouldn't appear in the top-level package until "
				+ "they've been loaded", Collections.emptyMap(), getModel()
				.getTopLevel().getModules());
		assertEquals(
				"Packages shouldn't appear in the top-level package until "
						+ "they've been loaded", Collections.emptyMap(),
				getModel().getTopLevel().getModules());
	}

	/**
	 * Even when nothing has been loaded explicitly, the top-level package
	 * should contain the Python builtins.
	 */
	@Test
	public void topLevelIncludesBuiltins() throws Throwable {
		assertTrue("Can't find builtin 'len()' function in the top-level "
				+ "package", getModel().getTopLevel().getFunctions()
				.containsKey("len"));
	}

	@Test
	public void loadTopLevelModule() throws Throwable {

		Module loadedModule = getModel().loadModule("my_module");

		assertTrue("Top-level module failed to load", loadedModule != null);
		assertEquals("The parent of 'my_module' isn't the top-level package",
				getModel().getTopLevel(), loadedModule.getParentScope());

		assertKeys("Loading a module that doesn't import anything must add "
				+ "that module and only that module to the runtime model",
				getModel().getTopLevel().getModules(), "my_module");
	}

	@Test
	public void loadDifferentTopLevelModules() throws Throwable {

		Module loadedModule = getModel().loadModule("my_module");

		Module loadedModule2 = getModel().loadModule("my_module2");

		assertTrue("Second top-level module failed to load",
				loadedModule2 != null);
		assertEquals("The parent of 'my_module2' isn't the top-level package",
				getModel().getTopLevel(), loadedModule2.getParentScope());

		assertNotSame(
				"Loading different modules must return different objects",
				loadedModule, loadedModule2);

		assertKeys("Loading two modules that don't import anything must "
				+ "load those modules and only those modules into the runtime "
				+ "model", getModel().getTopLevel().getModules(),
				"my_module", "my_module2");
	}

	@Test
	public void loadSameModuleTwice() throws Throwable {

		Module loadedModule = getModel().loadModule("my_module");

		Module loadedModule2 = getModel().loadModule("my_module");

		assertTrue("Second loading of the same failed", loadedModule2 != null);

		assertEquals("Loading same module must return the same object each "
				+ "time", loadedModule, loadedModule2);

		assertKeys("Loading a module that doesn't import anything must add "
				+ "that module and only that module to the runtime model",
				getModel().getTopLevel().getModules(), "my_module");
	}

	@Test
	public void loadTopLevelPackage() throws Throwable {

		Module loadedPackage = getModel().loadPackage("my_package");
		assertTrue("Top-level package failed to load", loadedPackage != null);

		assertEquals("The parent of 'my_package' isn't the top-level package",
				getModel().getTopLevel(), loadedPackage.getParentScope());

		assertKeys("Loading a package that doesn't import anything must add "
				+ "that package and only that package to the runtime model",
				getModel().getTopLevel().getModules(), "my_package");
	}

	@Test
	public void loadingTopLevelPackageDoesntLoadSubItems() throws Throwable {

		Module loadedPackage = getModel().loadPackage("my_package");
		assertTrue("Top-level package failed to load", loadedPackage != null);

		assertEquals("Loading a package that doesn't import anything "
				+ "mustn't load any submodules", Collections.emptyMap(),
				loadedPackage.getModules());
		assertEquals("Loading a package that doesn't import anything "
				+ "mustn't load any subpackages", Collections.emptyMap(),
				loadedPackage.getModules());
	}

	@Test
	public void subModules() throws Throwable {

		Module loadedModule = getModel()
				.loadModule("my_package.my_submodule");
		assertTrue("Submodule failed to load", loadedModule != null);

		Module parent = getModel().getTopLevel().getModules().get(
				"my_package");
		assertTrue("Loading a submodule must load any packages above it",
				parent != null);
		assertEquals("The parent of 'my_submodule' isn't 'my_package'", parent,
				loadedModule.getParentScope());

		assertKeys("Loading a submodule that doesn't import anything must "
				+ "add only that module and its parent packages to the "
				+ "runtime model", parent.getModules(), "my_submodule");
	}

	@Test
	public void subPackages() throws Throwable {

		Module loadedPackage = getModel().loadPackage(
				"my_package.my_subpackage");
		assertTrue("Subpackage failed to load", loadedPackage != null);

		Module parent = getModel().getTopLevel().getModules().get(
				"my_package");
		assertTrue("Loading a subpackage must load any packages above it",
				parent != null);
		assertEquals("The parent of 'my_subpackage' isn't 'my_package'",
				parent, loadedPackage.getParentScope());

		assertKeys("Loading a subpackage that doesn't import anything must "
				+ "add only that package and its parent packages to the "
				+ "runtime model", parent.getModules(), "my_subpackage");
	}

	@Test
	public void subSubModules() throws Throwable {

		Module loadedModule = getModel().loadModule(
				"my_package.my_subpackage.my_subsubmodule");
		assertTrue("Subsubmodule failed to load", loadedModule != null);

		Module parent = getModel().getTopLevel().getModules().get(
				"my_package");
		assertTrue("Loading a submodule must load any packages above it",
				parent != null);
		parent = parent.getModules().get("my_subpackage");
		assertTrue("Loading a submodule must load any packages above it",
				parent != null);

		assertEquals("The parent of 'my_subsubmodule' isn't 'my_subpackage'",
				parent, loadedModule.getParentScope());

		assertKeys("Loading a submodule that doesn't import anything must "
				+ "add only that module and its parent packages to the "
				+ "runtime model", parent.getModules(), "my_subsubmodule");
	}

	@Test
	public void subSubPackages() throws Throwable {

		Module loadedPackage = getModel().loadPackage(
				"my_package.my_subpackage.my_subsubpackage");
		assertTrue("Subsubpackage failed to load", loadedPackage != null);

		Module parent = getModel().getTopLevel().getModules().get(
				"my_package");
		assertTrue("Loading a subpackage must load any packages above it",
				parent != null);
		parent = parent.getModules().get("my_subpackage");
		assertTrue("Loading a submodule must load any packages above it",
				parent != null);

		assertEquals("The parent of 'my_subsubpackage' isn't 'my_subpackage'",
				parent, loadedPackage.getParentScope());

		assertKeys("Loading a subpackage that doesn't import anything must "
				+ "add only that package and its parent packages to the "
				+ "runtime model", parent.getModules(), "my_subsubpackage");
	}

	@Test
	public void failLoadModule() throws Throwable {
		Module loaded = getModel().loadModule("not_a_module");
		assertFalse("Loading non-existent module succeeded when it should "
				+ "have failed", loaded != null);
	}

	@Test
	public void failLoadPackage() throws Throwable {
		Module loaded = getModel().loadPackage("not_a_package");
		assertFalse("Loading non-existent package succeeded when it should "
				+ "have failed", loaded != null);
	}

	@Test
	public void failLoadModule2() throws Throwable {
		Module loaded = getModel().loadModule("a.b.c");
		assertFalse("Loading non-existent module succeeded when it should "
				+ "have failed", loaded != null);
	}

	@Test
	public void failLoadPackage2() throws Throwable {
		Module loaded = getModel().loadPackage("a.b.c");
		assertFalse("Loading non-existent package succeeded when it should "
				+ "have failed", loaded != null);
	}
}
