package uk.ac.ic.doc.gander.hierarchy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HierarchyLookupTest extends AbstractHierarchyTest {

	@Test
	public void moduleLookup() throws Throwable {
		SourceFile module = getHierarchy().findSourceFile("my_module1");
		SourceFile expectedModule = getHierarchy().getTopLevelPackage()
				.getSourceFiles().get("my_module1");

		assertEquals(expectedModule, module);

		module = getHierarchy().findSourceFile("my_module2");
		expectedModule = getHierarchy().getTopLevelPackage().getSourceFiles().get(
				"my_module2");

		assertEquals(expectedModule, module);
	}

	/**
	 * Test that empty string (which indicates top-level package) fails when
	 * looked up as a module.
	 */
	@Test
	public void moduleLookupError1() throws Throwable {
		assertEquals(null, getHierarchy().findSourceFile(""));
	}

	/**
	 * Test that a module that doesn't exist on disk fails on lookup.
	 */
	@Test
	public void moduleLookupError2() throws Throwable {
		assertEquals(null, getHierarchy().findSourceFile("blahkdfkj"));
	}

	/**
	 * Test that a file that exists but isn't a module fails on lookup.
	 */
	@Test
	public void moduleLookupError3() throws Throwable {
		assertEquals(null, getHierarchy().findSourceFile("not_a_module"));
	}

	@Test
	public void submoduleLookup() throws Throwable {
		SourceFile module = getHierarchy().findSourceFile("my_package.my_submodule");
		SourceFile expectedModule = getHierarchy().getTopLevelPackage()
				.getPackages().get("my_package").getSourceFiles().get(
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
