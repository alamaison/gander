package uk.ac.ic.doc.gander.hierarchy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HierarchyLookupTest extends AbstractHierarchyTest {

	@Test
	public void moduleLookup() throws Throwable {
		Module module = getHierarchy().findModule("my_module1");
		Module expectedModule = getHierarchy().getTopLevelPackage()
				.getModules().get("my_module1");

		assertEquals(expectedModule, module);

		module = getHierarchy().findModule("my_module2");
		expectedModule = getHierarchy().getTopLevelPackage().getModules().get(
				"my_module2");

		assertEquals(expectedModule, module);
	}

	/**
	 * Test that empty string (which indicates top-level package) fails when
	 * looked up as a module.
	 */
	@Test
	public void moduleLookupError1() throws Throwable {
		assertEquals(null, getHierarchy().findModule(""));
	}

	/**
	 * Test that a module that doesn't exist on disk fails on lookup.
	 */
	@Test
	public void moduleLookupError2() throws Throwable {
		assertEquals(null, getHierarchy().findModule("blahkdfkj"));
	}

	/**
	 * Test that a file that exists but isn't a module fails on lookup.
	 */
	@Test
	public void moduleLookupError3() throws Throwable {
		assertEquals(null, getHierarchy().findModule("not_a_module"));
	}

	@Test
	public void submoduleLookup() throws Throwable {
		Module module = getHierarchy().findModule("my_package.my_submodule");
		Module expectedModule = getHierarchy().getTopLevelPackage()
				.getPackages().get("my_package").getModules().get(
						"my_submodule");

		assertEquals(expectedModule, module);
	}

	@Test
	public void packageLookup() throws Throwable {
		Package pkg = getHierarchy().findPackage("my_package");
		Package expectedPackage = getHierarchy().getTopLevelPackage()
				.getPackages().get("my_package");
		assertEquals(expectedPackage, pkg);
	}
}
