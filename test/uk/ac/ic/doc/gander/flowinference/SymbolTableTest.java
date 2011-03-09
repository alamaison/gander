package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TPackage;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Importable;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

public class SymbolTableTest {

	private static final String TEST_FOLDER = "python_test_code/symbol_table";
	private Model model;
	private Module module;
	private SymbolTable table;

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);

		File topLevelDirectory = new File(topLevel.toURI());

		model = new Model(topLevelDirectory);
		module = model.getTopLevelPackage().getModules().get("start");
		table = new SymbolTable(model);
	}

	@Test
	public void localFunction() {
		assertTrue("start's symbol table doesn't contain 'alice'", table
				.symbols(module).containsKey("alice"));

		Type type = table.symbols(module).get("alice");
		assertTrue("start's symbol table contains 'alice' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function alice = module.getFunctions().get("alice");

		assertEquals("Type resolved to a function but not to 'alice'", alice,
				((TFunction) type).getFunctionInstance());

	}

	@Test
	public void localClass() {
		assertTrue("start's symbol table doesn't contain 'Bob'", table.symbols(
				module).containsKey("Bob"));

		Type type = table.symbols(module).get("Bob");
		assertTrue("start's symbol table contains 'Bob' but it isn't "
				+ "recognised as referring to a class", type instanceof TClass);

		Class bob = module.getClasses().get("Bob");

		assertEquals("Type resolved to a class but not to 'Bob'", bob,
				((TClass) type).getClassInstance());
	}

	@Test
	public void localClassMethod() {
		assertTrue("start.Bob's symbol table doesn't contain 'charles'", table
				.symbols(module.getClasses().get("Bob")).containsKey("charles"));

		Class bob = module.getClasses().get("Bob");

		Type type = table.symbols(bob).get("charles");
		assertTrue("start.Bob's symbol table contains 'charles' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function charles = bob.getFunctions().get("charles");

		assertEquals("Type resolved to a function but not to 'charles'",
				charles, ((TFunction) type).getFunctionInstance());
	}

	@Test
	public void siblingImport() {
		assertTrue("start's symbol table doesn't include 'gertrude'", table
				.symbols(module).containsKey("gertrude"));

		Type type = table.symbols(module).get("gertrude");
		assertTrue("start's symbol table contains 'gertrude' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module gertrude = model.lookupModule("gertrude");
		assertEquals("Type resolved to a module but not to 'gertrude'",
				gertrude, ((TModule) type).getModuleInstance());
	}

	/**
	 * Import a package (i.e. __init__.py) rather than a module.
	 */
	@Test
	public void packageImport() {
		assertTrue("start's symbol table doesn't contain 'stepchildren'", table
				.symbols(module).containsKey("stepchildren"));

		Type type = table.symbols(module).get("stepchildren");
		assertTrue("start's symbol table contains 'stepchildren' but it isn't "
				+ "recognised as referring to a package",
				type instanceof TPackage);

		Package stepchildren = model.lookupPackage("stepchildren");
		assertEquals("Type resolved to a package but not to 'stepchildren'",
				stepchildren, ((TPackage) type).getPackageInstance());
	}

	@Test
	public void childImport() {
		assertTrue("start's symbol table doesn't contain 'children'", table
				.symbols(module).containsKey("children"));

		Package children = model.getTopLevelPackage().getPackages().get(
				"children");
		assertTrue("Package 'children' not in model", children != null);

		Map<String, Type> childrenTable = table.symbols(children);
		assertTrue("No symbol table for top-level package 'children'",
				childrenTable != null);

		assertTrue("children's symbol table doesn't contain 'bobby'",
				childrenTable.containsKey("bobby"));

		Type type = table.symbols(children).get("bobby");
		assertTrue("children's symbol table contains 'bobby' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module bobby = children.getModules().get("bobby");
		assertEquals("Type resolved to a module but not to 'bobby'", bobby,
				((TModule) type).getModuleInstance());
	}

	/**
	 * Submodules can import their siblings just using their name rather than
	 * their fully qualified path.
	 */
	@Test
	public void relativeSiblingImport() {
		Module maggie = model.getTopLevelPackage().getPackages()
				.get("children").getModules().get("maggie");

		assertTrue("maggie's symbol table doesn't include 'bobby'", table
				.symbols(maggie).containsKey("bobby"));

		Type type = table.symbols(maggie).get("bobby");
		assertTrue("maggie's symbol table contains 'bobby' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module bobby = model.lookupModule("children.bobby");
		assertEquals("Type resolved to a module but not to 'bobby'", bobby,
				((TModule) type).getModuleInstance());
	}

	/**
	 * Submodules can import their nephews just using their name rather than
	 * their fully qualified path.
	 */
	@Test
	public void relativeNephewImport() {
		Module maggie = model.getTopLevelPackage().getPackages()
				.get("children").getModules().get("maggie");

		assertTrue("maggie's symbol table doesn't include 'children'", table
				.symbols(maggie).containsKey("children"));

		Type type = table.symbols(maggie).get("children");
		assertTrue("maggie's symbol table contains 'children' but it isn't "
				+ "recognised as referring to a package",
				type instanceof TPackage);

		Package children = model.lookupPackage("children.children");
		assertEquals(
				"Type resolved to a package but not to 'children.children'",
				children, ((TPackage) type).getPackageInstance());

		assertTrue("children.children's symbol table doesn't include "
				+ "'grandchild'", table.symbols(children).containsKey(
				"grandchild"));

		type = table.symbols(children).get("grandchild");
		assertTrue("children.children's symbol table contains 'grandchild' "
				+ "but it isn't recognised as referring to a module",
				type instanceof TModule);
	}

	/**
	 * Submodules can import their nephews just using their name rather than
	 * their fully qualified path.
	 */
	@Test
	public void relativeNephewImportAs() {
		Module maggie = model.getTopLevelPackage().getPackages()
				.get("children").getModules().get("maggie");

		assertTrue("maggie's symbol table doesn't include 'iris'", table
				.symbols(maggie).containsKey("iris"));

		Type type = table.symbols(maggie).get("iris");
		assertTrue("maggie's symbol table contains 'iris' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module grandchild = model.lookupModule("children.children.grandchild");
		assertEquals("Type resolved to a module but not to "
				+ "'children.children.grandchild'", grandchild,
				((TModule) type).getModuleInstance());
	}

	@Test
	public void importAs() {
		// import stepchildren.uglychild as william

		assertTrue("start's symbol table doesn't include 'william'", table
				.symbols(module).containsKey("william"));

		Type type = table.symbols(module).get("william");
		assertTrue("start's symbol table contains 'william' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module uglychild = model.lookupModule("stepchildren.uglychild");
		assertEquals("william's type resolved to a module but not to "
				+ "'stepchildren.uglychild'", uglychild, ((TModule) type)
				.getModuleInstance());

		Package stepchildren = model.lookupPackage("stepchildren");
		assertTrue("stepchildren's symbol table doesn't include 'uglychild'",
				table.symbols(stepchildren).containsKey("uglychild"));

		Type uglyType = table.symbols(stepchildren).get("uglychild");
		assertEquals("Type of 'stepchildren.uglychild' resolved to a module "
				+ "but not to 'uglychild'", uglychild, ((TModule) uglyType)
				.getModuleInstance());
	}

	@Test
	public void importNestedInFunction() {
		Module jake = model.lookupModule("adopted_children.jake");
		assertFalse("jake's symbol table includes 'adopted_children' "
				+ "but shouldn't as its import is nested in a function", table
				.symbols(jake).containsKey("adopted_children"));

		Function fatty = jake.getFunctions().get("fatty");
		assertTrue("fatty's symbol table doesn't include 'adopted_children'",
				table.symbols(fatty).containsKey("adopted_children"));

		Type type = table.symbols(fatty).get("adopted_children");
		assertTrue("fatty's symbol table contains 'adopted_children' but it "
				+ "isn't recognised as referring to a package",
				type instanceof TPackage);
		
		Package adopted = model.lookupPackage("adopted_children");
		assertEquals("Type resolved to a package but not to 'adopted_children'", adopted,
				((TPackage) type).getScopeInstance());
	}

	// ./children/maggie.py: import bobby
	// ./children/maggie.py: import children.grandchild
	// ./children/maggie.py: import children.grandchild as iris
	// ./start.py: import gertrude
	// ./start.py: import children.bobby
	// ./start.py: import stepchildren
	// ./start.py: import stepchildren.uglychild as william
	// ./adopted_children/jake.py/fatty: import adopted_children


	@Test
	public void symbolsTopLevel() {
		Importable module = model.lookup("");
		assertEquals(model.getTopLevelPackage(), module);
		assertSymbols(module);
	}

	@Test
	public void symbolsStart() {
		assertSymbols(module, "gertrude", "children", "stepchildren",
				"william", "alice", "Bob");
	}

	@Test
	public void symbolsGertrude() {
		Importable module = model.lookup("gertrude");
		assertSymbols(module, "harry", "Iris");
	}

	@Test
	public void symbolsChildren() {
		Importable module = model.lookup("children");
		assertSymbols(module, "bobby", "children");
	}

	@Test
	public void symbolsChildrenBobby() {
		Importable module = model.lookup("children.bobby");
		assertSymbols(module);
	}

	@Test
	public void symbolsChildrenMaggie() {
		Importable module = model.lookup("children.maggie");
		assertSymbols(module, "bobby", "children", "iris");
	}

	@Test
	public void symbolsChildrenChildren() {
		Importable module = model.lookup("children.children");
		assertSymbols(module, "grandchild");
	}

	@Test
	public void symbolsChildrenChildrenGrandchild() {
		Importable module = model.lookup("children.children.grandchild");
		assertSymbols(module);
	}

	@Test
	public void symbolsStepchildren() {
		Importable module = model.lookup("stepchildren");
		assertSymbols(module, "uglychild");
	}

	@Test
	public void symbolsStepchildrenUglyChild() {
		Importable module = model.lookup("stepchildren.uglycild");
		assertSymbols(module);
	}

	private void assertSymbols(Importable module, String... expected) {
		Set<String> ex = new HashSet<String>();
		for (String token : expected)
			ex.add(token);
		assertEquals("Symbols don't match expected", ex, table.symbols(module)
				.keySet());
	}
}
