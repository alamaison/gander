package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TPackage;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Importable;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Package;

public class SymbolTableTest {

	private static final String TEST_FOLDER = "python_test_code/symbol_table";
	private Model model;
	private Hierarchy hierarchy;

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);

		List<File> paths = new ArrayList<File>();
		for (String sysPath : queryPythonPath()) {
			paths.add(new File(sysPath));
		}
		paths.add(new File(topLevel.toURI()));

		hierarchy = new Hierarchy(paths);
		model = new Model(hierarchy);
	}

	private Map<String, Type> symbols(Namespace scope) throws Exception {
		return new SymbolTable(model).symbols(scope);
	}

	@Test
	public void localFunction() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'alice'", symbols(
				start).containsKey("alice"));

		Type type = symbols(start).get("alice");
		assertTrue("start's symbol table contains 'alice' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function alice = start.getFunctions().get("alice");

		assertEquals("Type resolved to a function but not to 'alice'", alice,
				((TFunction) type).getFunctionInstance());

	}

	@Test
	public void localClass() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'Bob'", symbols(start)
				.containsKey("Bob"));

		Type type = symbols(start).get("Bob");
		assertTrue("start's symbol table contains 'Bob' but it isn't "
				+ "recognised as referring to a class", type instanceof TClass);

		Class bob = start.getClasses().get("Bob");

		assertEquals("Type resolved to a class but not to 'Bob'", bob,
				((TClass) type).getClassInstance());
	}

	@Test
	public void localClassMethod() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start.Bob's symbol table doesn't contain 'charles'",
				symbols(start.getClasses().get("Bob")).containsKey("charles"));

		Class bob = start.getClasses().get("Bob");

		Type type = symbols(bob).get("charles");
		assertTrue("start.Bob's symbol table contains 'charles' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function charles = bob.getFunctions().get("charles");

		assertEquals("Type resolved to a function but not to 'charles'",
				charles, ((TFunction) type).getFunctionInstance());
	}

	@Test
	public void siblingImport() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't include 'gertrude'", symbols(
				start).containsKey("gertrude"));

		Type type = symbols(start).get("gertrude");
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
	public void packageImport() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'stepchildren'",
				symbols(start).containsKey("stepchildren"));

		Type type = symbols(start).get("stepchildren");
		assertTrue("start's symbol table contains 'stepchildren' but it isn't "
				+ "recognised as referring to a package",
				type instanceof TPackage);

		Package stepchildren = model.lookupPackage("stepchildren");
		assertEquals("Type resolved to a package but not to 'stepchildren'",
				stepchildren, ((TPackage) type).getPackageInstance());
	}

	@Test
	public void childImport() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'children'", symbols(
				start).containsKey("children"));

		Package children = model.getTopLevelPackage().getPackages().get(
				"children");
		assertTrue("Package 'children' not in model", children != null);

		Map<String, Type> childrenTable = symbols(children);
		assertTrue("No symbol table for top-level package 'children'",
				childrenTable != null);

		assertTrue("children's symbol table doesn't contain 'bobby'",
				childrenTable.containsKey("bobby"));

		Type type = symbols(children).get("bobby");
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
	public void relativeSiblingImport() throws Throwable {
		Module maggie = model.loadModule("children.maggie");

		assertTrue("maggie's symbol table doesn't include 'bobby'", symbols(
				maggie).containsKey("bobby"));

		Type type = symbols(maggie).get("bobby");
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
	public void relativeNephewImport() throws Throwable {
		Module maggie = model.loadModule("children.maggie");

		assertTrue("maggie's symbol table doesn't include 'children'", symbols(
				maggie).containsKey("children"));

		Type type = symbols(maggie).get("children");
		assertTrue("maggie's symbol table contains 'children' but it isn't "
				+ "recognised as referring to a package",
				type instanceof TPackage);

		Package children = model.lookupPackage("children.children");
		assertEquals(
				"Type resolved to a package but not to 'children.children'",
				children, ((TPackage) type).getPackageInstance());

		assertTrue("children.children's symbol table doesn't include "
				+ "'grandchild'", symbols(children).containsKey("grandchild"));

		type = symbols(children).get("grandchild");
		assertTrue("children.children's symbol table contains 'grandchild' "
				+ "but it isn't recognised as referring to a module",
				type instanceof TModule);
	}

	/**
	 * Submodules can import their nephews just using their name rather than
	 * their fully qualified path.
	 */
	@Test
	public void relativeNephewImportAs() throws Throwable {
		Module maggie = model.loadModule("children.maggie");

		assertTrue("maggie's symbol table doesn't include 'iris'", symbols(
				maggie).containsKey("iris"));

		Type type = symbols(maggie).get("iris");
		assertTrue("maggie's symbol table contains 'iris' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module grandchild = model.lookupModule("children.children.grandchild");
		assertEquals("Type resolved to a module but not to "
				+ "'children.children.grandchild'", grandchild,
				((TModule) type).getModuleInstance());
	}

	@Test
	public void importAs() throws Throwable {
		// import stepchildren.uglychild as william

		Module start = model.loadModule("start");

		assertTrue("start's symbol table doesn't include 'william'", symbols(
				start).containsKey("william"));

		Type type = symbols(start).get("william");
		assertTrue("start's symbol table contains 'william' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module uglychild = model.lookupModule("stepchildren.uglychild");
		assertEquals("william's type resolved to a module but not to "
				+ "'stepchildren.uglychild'", uglychild, ((TModule) type)
				.getModuleInstance());

		Package stepchildren = model.lookupPackage("stepchildren");
		assertTrue("stepchildren's symbol table doesn't include 'uglychild'",
				symbols(stepchildren).containsKey("uglychild"));

		Type uglyType = symbols(stepchildren).get("uglychild");
		assertEquals("Type of 'stepchildren.uglychild' resolved to a module "
				+ "but not to 'uglychild'", uglychild, ((TModule) uglyType)
				.getModuleInstance());
	}

	@Test
	public void importNestedInFunction() throws Throwable {
		Module jake = model.loadModule("adopted_children.jake");
		assertFalse("jake's symbol table includes 'adopted_children' "
				+ "but shouldn't as its import is nested in a function",
				symbols(jake).containsKey("adopted_children"));

		Function fatty = jake.getFunctions().get("fatty");
		assertTrue("fatty's symbol table doesn't include 'adopted_children'",
				symbols(fatty).containsKey("adopted_children"));

		Type type = symbols(fatty).get("adopted_children");
		assertTrue("fatty's symbol table contains 'adopted_children' but it "
				+ "isn't recognised as referring to a package",
				type instanceof TPackage);

		Package adopted = model.lookupPackage("adopted_children");
		assertEquals(
				"Type resolved to a package but not to 'adopted_children'",
				adopted, ((TPackage) type).getNamespaceInstance());
	}

	@Test
	public void fromImportFirstItem() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'commands'", symbols(
				fetch).containsKey("commands"));

		Type type = symbols(fetch).get("commands");
		assertTrue("fetch's symbol table contains 'commands' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module commands = model.lookupModule("mercurial.commands");
		assertEquals("Type resolved to a module but not to "
				+ "'mercurial.commands'", commands, ((TModule) type)
				.getModuleInstance());
	}

	@Test
	public void fromImportSubsequentItem() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'hg'", symbols(fetch)
				.containsKey("hg"));

		Type type = symbols(fetch).get("hg");
		assertTrue("fetch's symbol table contains 'hg' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module hg = model.lookupModule("mercurial.hg");
		assertEquals("Type resolved to a module but not to 'mercurial.hg'", hg,
				((TModule) type).getModuleInstance());
	}

	@Test
	public void fromImportAs() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'droid'", symbols(
				fetch).containsKey("droid"));

		Type type = symbols(fetch).get("droid");
		assertTrue("fetch's symbol table contains 'droid' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module droid = model.lookupModule("mercurial.zorg");
		assertEquals("Type resolved to a module but not to 'mercurial.droid'",
				droid, ((TModule) type).getModuleInstance());
	}

	/**
	 * Imports module from a relative package.
	 */
	@Test
	public void fromImportModuleFromPackageRelative() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'me'", symbols(fetch)
				.containsKey("me"));

		Type type = symbols(fetch).get("me");
		assertTrue("fetch's symbol table contains 'me' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module me = model.lookupModule("hgext.catch.me");
		assertEquals("Type resolved to a module but not to "
				+ "'hgext.catch.me'", me, ((TModule) type).getModuleInstance());
	}

	/**
	 * Imports function from a relative module.
	 */
	@Test
	public void fromImportFunctionFromModuleRelative() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'hamstring'", symbols(
				fetch).containsKey("hamstring"));

		Type type = symbols(fetch).get("hamstring");
		assertTrue("fetch's symbol table contains 'hamstring' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function hamstring = model.lookupModule("hgext.stretch").getFunctions()
				.get("hamstring");
		assertEquals("Type resolved to a function but not to "
				+ "'hamstring' in 'hgext.stretch'", hamstring,
				((TFunction) type).getFunctionInstance());
	}

	@Test
	public void libraryImport() throws Throwable {
		Module libby = model.loadModule("libby");
		assertTrue("libby's symbol table doesn't contain 'base64'", symbols(
				libby).containsKey("base64"));

		Module base64 = model.getTopLevelPackage().getModules().get("base64");
		assertTrue("Package 'base64' not in model", base64 != null);

		Map<String, Type> base64Table = symbols(base64);
		assertTrue("No symbol table for library package 'base64'",
				base64Table != null);

		assertTrue("base64's symbol table doesn't contain 'b64encode'",
				base64Table.containsKey("b64encode"));

		Type type = symbols(base64).get("b64encode");
		assertTrue("base64's symbol table contains 'b64encode' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function b64encode = base64.getFunctions().get("b64encode");
		assertEquals("Type resolved to a function but not to 'b64encode'",
				b64encode, ((TFunction) type).getFunctionInstance());
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
	public void symbolsTopLevel() throws Throwable {
		Importable module = model.load("");
		assertEquals(model.getTopLevelPackage(), module);
		assertSymbols(module, "abs", "all", "any", "apply", "basestring",
				"bin", "bool", "buffer", "bytearray", "bytes", "callable",
				"chr", "classmethod", "cmp", "coerce", "compile", "complex",
				"copyright", "credits", "delattr", "dict", "dir", "divmod",
				"enumerate", "eval", "execfile", "exit", "file", "filter",
				"float", "format", "frozenset", "getattr", "globals",
				"hasattr", "hash", "help", "hex", "id", "input", "int",
				"intern", "isinstance", "issubclass", "iter", "len", "license",
				"list", "locals", "long", "map", "max", "min", "next",
				"object", "oct", "open", "ord", "pow", "property", "quit",
				"range", "raw_input", "reduce", "reload", "repr", "reversed",
				"round", "set", "setattr", "slice", "sorted", "staticmethod",
				"str", "sum", "super", "tuple", "type", "unichr", "unicode",
				"vars", "xrange", "zip");
	}

	@Test
	public void symbolsStart() throws Throwable {
		Importable module = model.load("start");
		assertSymbols(module, "gertrude", "children", "stepchildren",
				"william", "alice", "Bob");
	}

	@Test
	public void symbolsGertrude() throws Throwable {
		Importable module = model.load("gertrude");
		assertSymbols(module, "harry", "Iris");
	}

	@Test
	public void symbolsChildren() throws Throwable {
		Importable module = model.load("children");
		assertSymbols(module);
	}

	@Test
	public void symbolsChildrenBobby() throws Throwable {
		Importable module = model.load("children.bobby");
		assertSymbols(module);
	}

	@Test
	public void symbolsChildrenMaggie() throws Throwable {
		Importable module = model.load("children.maggie");
		assertSymbols(module, "bobby", "children", "iris");
	}

	@Test
	public void symbolsChildrenChildren() throws Throwable {
		Importable module = model.load("children.children");
		assertSymbols(module);
	}

	@Test
	public void symbolsChildrenChildrenGrandchild() throws Throwable {
		Importable module = model.load("children.children.grandchild");
		assertSymbols(module);
	}

	@Test
	public void symbolsStepchildren() throws Throwable {
		Importable module = model.load("stepchildren");
		assertSymbols(module);
	}

	@Test
	public void symbolsStepchildrenUglyChild() throws Throwable {
		Importable module = model.load("stepchildren.uglycild");
		assertSymbols(module);
	}

	private void assertSymbols(Importable module, String... expected)
			throws Exception {
		Set<String> ex = new HashSet<String>();
		for (String token : expected)
			ex.add(token);
		assertEquals("Symbols don't match expected", ex, symbols(module)
				.keySet());
	}

	private static final String PYTHON_PATH_PROGRAM = "import sys\n"
			+ "for x in sys.path:\n" + "    print x\n\n";

	private Iterable<String> queryPythonPath() {
		try {
			String[] commands = { "python", "-c", PYTHON_PATH_PROGRAM };
			Process python = Runtime.getRuntime().exec(commands);
			InputStream output = python.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					output));

			List<String> path = new ArrayList<String>();

			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				path.add(line);
			}
			return path;
		} catch (IOException e) {
			// If we fail because, for instance Python doesn't exist on the
			// system use empty Python path.
			return Collections.emptyList();
		}
	}
}
