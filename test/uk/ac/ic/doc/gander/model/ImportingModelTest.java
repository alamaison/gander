package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

/**
 * Test loading modules and packages that don't import anything else.
 */
public class ImportingModelTest extends AbstractModelTest {

	@Before
	public void setup() throws Throwable {
		createTestModel(IMPORTING_PROJ);
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
				.getTopLevelPackage() != null);
		assertEquals("Modules shouldn't appear in the top-level package until "
				+ "they've been loaded", Collections.emptyMap(), getModel()
				.getTopLevelPackage().getModules());
		assertEquals(
				"Packages shouldn't appear in the top-level package until "
						+ "they've been loaded", Collections.emptyMap(),
				getModel().getTopLevelPackage().getPackages());
	}

	@Test
	public void siblingModuleImport() throws Throwable {
		String start = "import_sibling_module";

		getModel().loadModule(start);

		assertKeys("Loading a module that imports a sibling must add "
				+ "that module and the imported sibling to the runtime model",
				getModel().getTopLevelPackage().getModules(), start, "sibling");
	}

	@Test
	public void fromSiblingImport() throws Throwable {
		String start = "from_sibling_import";

		getModel().loadModule(start);

		assertKeys("Loading a module that imports something from a sibling "
				+ "must add that module and the sibling module to the "
				+ "runtime model",
				getModel().getTopLevelPackage().getModules(), start, "sibling");
	}

	@Test
	public void fromDistantImport() throws Throwable {
		String start = "relative.fred.from_distant_import";

		getModel().loadModule(start);

		assertKeys("Loading a module must add that module to the "
				+ "runtime model", getModel().lookupPackage("relative.fred")
				.getModules(), "from_distant_import");

		assertKeys("Loading a module must load its parent packages", getModel()
				.getTopLevelPackage().getPackages(), "relative");

		assertKeys("Loading a module that imports something from another "
				+ "module must add that module to the runtime model",
				getModel().getTopLevelPackage().getModules(), "sibling");
	}

	@Test
	public void fromDistantPackageImport() throws Throwable {
		String start = "relative.fred.from_distant_package_import";

		getModel().loadModule(start);

		assertKeys("Loading a module must add that module to the "
				+ "runtime model", getModel().lookupPackage("relative.fred")
				.getModules(), "from_distant_package_import");

		assertKeys("Loading a module that imports a package from another "
				+ "package must add the source package to the runtime model",
				getModel().getTopLevelPackage().getPackages(), "relative",
				"sibling_package");

		assertKeys("Loading a module that imports a package from another "
				+ "package must add the target package to the runtime model",
				getModel().lookupPackage("sibling_package").getPackages(),
				"child_package");
	}

	@Test
	public void siblingPackageImport() throws Throwable {
		String start = "import_sibling_package";

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().getTopLevelPackage().getModules(), start);
		assertKeys("Loading a module that imports a sibling must add "
				+ "that sibling to the runtime model", getModel()
				.getTopLevelPackage().getPackages(), "sibling_package");
	}

	/**
	 * As-importing should have exactly the same effect on the runtime model as
	 * regular importing. Only the symbol table will have a difference.
	 */
	@Test
	public void siblingModuleImportAs() throws Throwable {
		String start = "import_sibling_module_as";

		getModel().loadModule(start);

		assertKeys("Loading a module that imports a sibling must add "
				+ "that module and the imported sibling to the runtime model",
				getModel().getTopLevelPackage().getModules(), start, "sibling");
	}

	/**
	 * As-importing should have exactly the same effect on the runtime model as
	 * regular importing. Only the symbol table will have a difference.
	 */
	@Test
	public void siblingPackageImportAs() throws Throwable {
		String start = "import_sibling_package_as";

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().getTopLevelPackage().getModules(), start);
		assertKeys("Loading a module that imports a sibling must add "
				+ "that sibling to the runtime model", getModel()
				.getTopLevelPackage().getPackages(), "sibling_package");
	}

	@Test
	public void submoduleImport() throws Throwable {
		String start = "import_nephew_module";

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().getTopLevelPackage().getModules(), start);

		assertKeys("Loading a module that imports a nephew must add the "
				+ "nephew's parent package to the runtime model", getModel()
				.getTopLevelPackage().getPackages(), "sibling_package");

		assertKeys(
				"Loading a module that imports a nephew must add the nephew "
						+ "to the runtime model", getModel().lookupPackage(
						"sibling_package").getModules(), "child_module");
	}

	@Test
	public void subpackageImport() throws Throwable {
		String start = "import_nephew_package";

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().getTopLevelPackage().getModules(), start);

		assertKeys("Loading a module that imports a nephew must add the "
				+ "nephew's parent package to the runtime model", getModel()
				.getTopLevelPackage().getPackages(), "sibling_package");

		assertKeys(
				"Loading a module that imports a nephew must add the nephew "
						+ "to the runtime model", getModel().lookupPackage(
						"sibling_package").getPackages(), "child_package");
	}

	@Test
	public void relativeModuleImportDown() throws Throwable {
		String start = "relative.import_relative_module_down";

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().lookupPackage("relative").getModules(),
				"import_relative_module_down");

		assertKeys("Loading a module that imports a module relative to its "
				+ "package must add the relative module to the runtime model",
				getModel().lookupPackage("relative.fred").getModules(), "jack");
	}

	@Test
	public void relativePackageImportDown() throws Throwable {
		String start = "relative.import_relative_package_down";

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().lookupPackage("relative").getModules(),
				"import_relative_package_down");

		assertKeys("Loading a module that imports a module relative to its "
				+ "package must add the relative module to the runtime model",
				getModel().lookupPackage("relative.fred").getPackages(),
				"susan");
	}

	@Test
	public void relativeModuleImportUp() throws Throwable {
		String start = "relative.import_relative_module_up";

		assertTrue("The imported module musn't appear in the runtime model "
				+ "until the submodule that imports it is loaded", getModel()
				.lookupPackage("").getModules().isEmpty());

		getModel().loadModule(start);

		assertKeys(
				"Loading a module must add that module to the runtime model",
				getModel().lookupPackage("relative").getModules(),
				"import_relative_module_up");

		assertKeys("Loading a non-top-level module that imports a top-level "
				+ "module must add the top-level module to the runtime model",
				getModel().lookupPackage("").getModules(), "sibling");
	}

	@Test
	public void relativePackageImportUp() throws Throwable {
		String start = "relative.import_relative_package_up";

		assertTrue("The imported package musn't appear in the runtime model "
				+ "until the submodule that imports it is loaded", getModel()
				.lookupPackage("").getModules().isEmpty());

		getModel().loadModule(start);

		assertKeys("Loading a module must add that module to the runtime "
				+ "model", getModel().lookupPackage("relative").getModules(),
				"import_relative_package_up");

		assertKeys("Loading a non-top-level module that imports a "
				+ "top-level package must add the top-level package to the "
				+ "runtime model", getModel().lookupPackage("").getPackages(),
				"relative", "sibling_package");
	}

	@Test
	public void relativeModuleImportUpThenDown() throws Throwable {
		String start = "relative.import_relative_module_up_then_down";

		assertTrue("The imported module musn't appear in the runtime model "
				+ "until the submodule that imports it is loaded", getModel()
				.lookupPackage("").getPackages().isEmpty());

		getModel().loadModule(start);

		assertKeys("Loading a module must add that module to the runtime "
				+ "model", getModel().lookupPackage("relative").getModules(),
				"import_relative_module_up_then_down");

		assertKeys("Loading a non-top-level module that imports a "
				+ "non-top-level from a different top-level package must add "
				+ "the imported module to the runtime model", getModel()
				.lookupPackage("sibling_package").getModules(), "child_module");
	}

	@Test
	public void circularImport() throws Throwable {
		getModel().loadModule("circular1");

		assertKeys("Loading a module that imports another must add both to "
				+ "the runtime model", getModel().getTopLevelPackage()
				.getModules(), "circular1", "circular2");
	}
}
