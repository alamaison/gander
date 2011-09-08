package uk.ac.ic.doc.gander.hierarchy;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class HierarchyTest extends AbstractHierarchyTest {

	@Test
	public void topLevelPackages() throws Throwable {
		Map<String, Package> packages = getHierarchy().getTopLevelPackage()
				.getPackages();

		assertKeys(packages, "my_package", "email");
		assertEquals("my_package", packages.get("my_package").getName());

		assertEquals("The parent of 'my_package' isn't the top-level package",
				getHierarchy().getTopLevelPackage(), packages.get("my_package")
						.getParentPackage());
	}

	@Test
	public void topLevelModules() throws Throwable {
		Map<String, SourceFile> modules = getHierarchy().getTopLevelPackage()
				.getSourceFiles();

		assertKeys(modules, "my_module1", "my_module2", "os");

		assertEquals("The parent of 'my_module1' isn't the top-level package",
				getHierarchy().getTopLevelPackage(), modules.get("my_module1")
						.getParentPackage());
	}

	@Test
	public void packageModules() throws Throwable {
		Map<String, SourceFile> modules = getHierarchy().getTopLevelPackage()
				.getPackages().get("my_package").getSourceFiles();

		assertKeys(modules, "my_submodule");

		assertEquals("The parent of 'my_submodule' isn't 'my_package'",
				getHierarchy().getTopLevelPackage().getPackages().get(
						"my_package"), modules.get("my_submodule")
						.getParentPackage());
	}
}
