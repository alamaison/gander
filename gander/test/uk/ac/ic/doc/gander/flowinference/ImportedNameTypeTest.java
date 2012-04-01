package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public class ImportedNameTypeTest {

	private static final String TEST_FOLDER = "python_test_code/imported_names";
	private MutableModel model;
	private Hierarchy hierarchy;

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);
		hierarchy = HierarchyFactory
				.createHierarchy(new File(topLevel.toURI()));
		model = new DefaultModel(hierarchy);
	}

	private class TypeGetter {

		private final CodeObject scope;
		private final ZeroCfaTypeEngine engine;

		private final class Singletoniser implements Transformer<Type, Type> {

			@Override
			public Type transformFiniteResult(java.util.Set<Type> result) {
				if (result.size() == 1) {
					return result.iterator().next();
				} else {
					throw new AssertionError(
							"Not a singleton; all tests assume "
									+ "singleton type result:" + result);
				}
			}

			@Override
			public Type transformInfiniteResult() {
				throw new AssertionError("Infinite result; not a singleton; "
						+ "all tests assume singleton type result");
			}
		}

		TypeGetter(MutableModel model, CodeObject scope) {

			this.engine = new ZeroCfaTypeEngine();
			this.scope = scope;
		}

		Type typeOf(String variableName) {
			Result<Type> result = engine.typeOf(new Variable(variableName,
					scope));
			return result.transformResult(new Singletoniser());
		}

		boolean typeExistsFor(String variableName) {
			Result<Type> result = engine.typeOf(new Variable(variableName,
					scope));

			return result.transformResult(new Transformer<Type, Boolean>() {

				@Override
				public Boolean transformFiniteResult(Set<Type> result) {
					return !result.isEmpty();
				}

				@Override
				public Boolean transformInfiniteResult() {
					return true;
				}
			});
		}
	}

	private TypeGetter typer(OldNamespace scope) throws Exception {
		return new TypeGetter(model, scope.codeObject());
	}

	@Test
	public void localFunction() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'alice'", typer(start)
				.typeExistsFor("alice"));

		Type type = typer(start).typeOf("alice");
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
		assertTrue("start's symbol table doesn't contain 'Bob'", typer(start)
				.typeExistsFor("Bob"));

		Type type = typer(start).typeOf("Bob");
		assertTrue("start's symbol table contains 'Bob' but it isn't "
				+ "recognised as referring to a class", type instanceof TClass);

		Class bob = start.getClasses().get("Bob");

		assertEquals("Type resolved to a class but not to 'Bob'", bob,
				((TClass) type).getClassInstance());
	}

	/**
	 * Symbols bound by a nested import should only exist in the scope of the
	 * import.
	 */
	@Test
	public void nestedImport() throws Throwable {
		Module start = model.loadModule("start");

		Function charles = start.getClasses().get("Bob").getFunctions()
				.get("charles");

		// import mercurial

		assertTrue("start.Bob.charles's symbol table should contain "
				+ "'mercurial' imported locally",
				typer(charles).typeExistsFor("mercurial"));

		Type type = typer(charles).typeOf("mercurial");
		assertTrue("start.Bob.charles's symbol table contains 'mercurial' but"
				+ " it isn't recognised as referring to a module",
				type instanceof TModule);

		Module mercurial = model.lookup("mercurial");
		assertEquals("Type resolved to a module but not to 'mercurial'",
				mercurial, ((TModule) type).getModuleInstance());

		// from gertrude import Iris

		assertTrue("start.Bob.charles's symbol table should contain "
				+ "'Iris' imported locally",
				typer(charles).typeExistsFor("Iris"));

		type = typer(charles).typeOf("Iris");
		assertTrue("start.Bob.charles's symbol table contains 'Iris' but"
				+ " it isn't recognised as referring to a class",
				type instanceof TClass);

		Class iris = model.lookup("gertrude").getClasses().get("Iris");
		assertEquals("Type resolved to a class but not to 'Iris'", iris,
				((TClass) type).getClassInstance());
	}

	@Test
	public void siblingImport() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't include 'gertrude'",
				typer(start).typeExistsFor("gertrude"));

		Type type = typer(start).typeOf("gertrude");
		assertTrue("start's symbol table contains 'gertrude' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module gertrude = model.lookup("gertrude");
		assertEquals("Type resolved to a module but not to 'gertrude'",
				gertrude, ((TModule) type).getModuleInstance());
	}

	/**
	 * Import a package (i.e. __init__.py) rather than a module.
	 * 
	 * It should still be recognised as a module.
	 */
	@Test
	public void packageImport() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'stepchildren'",
				typer(start).typeExistsFor("stepchildren"));

		Type type = typer(start).typeOf("stepchildren");
		assertTrue("start's symbol table contains 'stepchildren' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module stepchildren = model.lookup("stepchildren");
		assertEquals("Type resolved to a module but not to 'stepchildren'",
				stepchildren, ((TModule) type).getModuleInstance());

		assertTrue("stepchildren's symbol table doesn't contain 'gertrude'",
				typer(stepchildren).typeExistsFor("gertrude"));

		type = typer(stepchildren).typeOf("gertrude");
		assertTrue("stepchildren's symbol table contains 'gertrude' but"
				+ " it isn't recognised as referring to a module",
				type instanceof TModule);

		Module gertrude = model.lookup("gertrude");
		assertEquals("Type resolved to a module but not to 'gertrude'",
				gertrude, ((TModule) type).getModuleInstance());
	}

	@Test
	public void childImport() throws Throwable {
		Module start = model.loadModule("start");
		assertTrue("start's symbol table doesn't contain 'children'",
				typer(start).typeExistsFor("children"));

		Module children = model.getTopLevel().getModules().get("children");
		assertTrue("Module 'children' not in model", children != null);

		assertTrue("children's symbol table doesn't contain 'bobby'",
				typer(children).typeExistsFor("bobby"));

		Type type = typer(children).typeOf("bobby");
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

		assertTrue("maggie's symbol table doesn't include 'bobby'",
				typer(maggie).typeExistsFor("bobby"));

		Type type = typer(maggie).typeOf("bobby");
		assertTrue("maggie's symbol table contains 'bobby' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module bobby = model.lookup("children.bobby");
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

		assertTrue("maggie's symbol table doesn't include 'children'",
				typer(maggie).typeExistsFor("children"));

		Type type = typer(maggie).typeOf("children");
		assertTrue("maggie's symbol table contains 'children' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module children = model.lookup("children.children");
		assertEquals(
				"Type resolved to a package but not to 'children.children'",
				children, ((TModule) type).getModuleInstance());

		assertTrue("children.children's symbol table doesn't include "
				+ "'grandchild'", typer(children).typeExistsFor("grandchild"));

		type = typer(children).typeOf("grandchild");
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

		assertTrue("maggie's symbol table doesn't include 'iris'",
				typer(maggie).typeExistsFor("iris"));

		Type type = typer(maggie).typeOf("iris");
		assertTrue("maggie's symbol table contains 'iris' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module grandchild = model.lookup("children.children.grandchild");
		assertEquals("Type resolved to a module but not to "
				+ "'children.children.grandchild'", grandchild,
				((TModule) type).getModuleInstance());
	}

	@Test
	public void importAs() throws Throwable {
		// import stepchildren.uglychild as william

		Module start = model.loadModule("start");

		assertTrue("start's symbol table doesn't include 'william'",
				typer(start).typeExistsFor("william"));

		Type type = typer(start).typeOf("william");
		assertTrue("start's symbol table contains 'william' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module uglychild = model.lookup("stepchildren.uglychild");
		assertEquals("william's type resolved to a module but not to "
				+ "'stepchildren.uglychild'", uglychild,
				((TModule) type).getModuleInstance());

		Module stepchildren = model.lookup("stepchildren");
		assertTrue("stepchildren's symbol table doesn't include 'uglychild'",
				typer(stepchildren).typeExistsFor("uglychild"));

		Type uglyType = typer(stepchildren).typeOf("uglychild");
		assertEquals("Type of 'stepchildren.uglychild' resolved to a module "
				+ "but not to 'uglychild'", uglychild,
				((TModule) uglyType).getModuleInstance());
	}

	@Test
	public void importNestedInFunction() throws Throwable {
		Module jake = model.loadModule("adopted_children.jake");
		assertFalse("jake's symbol table includes 'adopted_children' "
				+ "but shouldn't as its import is nested in a function",
				typer(jake).typeExistsFor("adopted_children"));

		Function fatty = jake.getFunctions().get("fatty");
		assertTrue("fatty's symbol table doesn't include 'adopted_children'",
				typer(fatty).typeExistsFor("adopted_children"));

		Type type = typer(fatty).typeOf("adopted_children");
		assertTrue("fatty's symbol table contains 'adopted_children' but it "
				+ "isn't recognised as referring to a module",
				type instanceof TModule);

		Module adopted = model.lookup("adopted_children");
		assertEquals("Type resolved to a module but not to 'adopted_children'",
				adopted, ((TModule) type).getModuleInstance());
	}

	@Test
	public void fromImportFirstItem() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'commands'",
				typer(fetch).typeExistsFor("commands"));

		Type type = typer(fetch).typeOf("commands");
		assertTrue("fetch's symbol table contains 'commands' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module commands = model.lookup("mercurial.commands");
		assertEquals("Type resolved to a module but not to "
				+ "'mercurial.commands'", commands,
				((TModule) type).getModuleInstance());
	}

	@Test
	public void fromImportSubsequentItem() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'hg'", typer(fetch)
				.typeExistsFor("hg"));

		Type type = typer(fetch).typeOf("hg");
		assertTrue("fetch's symbol table contains 'hg' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module hg = model.lookup("mercurial.hg");
		assertEquals("Type resolved to a module but not to 'mercurial.hg'", hg,
				((TModule) type).getModuleInstance());
	}

	@Test
	public void fromImportAs() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'droid'", typer(fetch)
				.typeExistsFor("droid"));

		Type type = typer(fetch).typeOf("droid");
		assertTrue("fetch's symbol table contains 'droid' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module droid = model.lookup("mercurial.zorg");
		assertEquals("Type resolved to a module but not to 'mercurial.droid'",
				droid, ((TModule) type).getModuleInstance());
	}

	/**
	 * Imports module from a relative package.
	 */
	@Test
	public void fromImportModuleFromPackageRelative() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'me'", typer(fetch)
				.typeExistsFor("me"));

		Type type = typer(fetch).typeOf("me");
		assertTrue("fetch's symbol table contains 'me' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module me = model.lookup("hgext.catch.me");
		assertEquals("Type resolved to a module but not to "
				+ "'hgext.catch.me'", me, ((TModule) type).getModuleInstance());
	}

	/**
	 * Imports function from a relative module.
	 */
	@Test
	public void fromImportFunctionFromModuleRelative() throws Throwable {
		Module fetch = model.loadModule("hgext.fetch");

		assertTrue("fetch's symbol table doesn't include 'hamstring'",
				typer(fetch).typeExistsFor("hamstring"));

		Type type = typer(fetch).typeOf("hamstring");
		assertTrue("fetch's symbol table contains 'hamstring' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function hamstring = model.lookup("hgext.stretch").getFunctions()
				.get("hamstring");
		assertEquals("Type resolved to a function but not to "
				+ "'hamstring' in 'hgext.stretch'", hamstring,
				((TFunction) type).getFunctionInstance());
	}

	@Test
	public void libraryImport() throws Throwable {
		Module libby = model.loadModule("libby");
		assertTrue("libby's symbol table doesn't contain 'base64'",
				typer(libby).typeExistsFor("base64"));

		Module base64 = model.getTopLevel().getModules().get("base64");
		assertTrue("SourceFile 'base64' not in model", base64 != null);

		assertTrue("base64's symbol table doesn't contain 'b64encode'",
				typer(base64).typeExistsFor("b64encode"));

		Type type = typer(base64).typeOf("b64encode");
		assertTrue("base64's symbol table contains 'b64encode' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function b64encode = base64.getFunctions().get("b64encode");
		assertEquals("Type resolved to a function but not to 'b64encode'",
				b64encode, ((TFunction) type).getFunctionInstance());
	}

	/**
	 * Imports should be found whereever they are, even if that means traversing
	 * down into the AST a bit. For example, an import in a try/catch.
	 */
	@Test
	public void astTraversalImport() throws Throwable {
		Module start = model.loadModule("traversal");
		assertTrue("traversal's symbol table doesn't include 'p'", typer(start)
				.typeExistsFor("p"));

		Type type = typer(start).typeOf("p");
		assertTrue("start's symbol table contains 'p' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module gertrude = model.lookup("gertrude");
		assertEquals("'p' resolved to a module but not to 'gertrude'",
				gertrude, ((TModule) type).getModuleInstance());

		assertFalse("traversal's symbol table mustn't include 'gertrude'",
				typer(start).typeExistsFor("gertrude"));
	}

	/**
	 * Although imports should be found by traversing the AST, this shouldn't
	 * include imports nested in functions or classes because these have their
	 * own tables and are processes separately.
	 */
	@Test
	public void astNonTraversalImport() throws Throwable {
		Module start = model.loadModule("traversal");

		assertFalse("traversal's symbol table mustn't include 'q'",
				typer(start).typeExistsFor("q"));
		assertFalse("traversal's symbol table mustn't include 'r'",
				typer(start).typeExistsFor("r"));

		assertFalse("traversal's symbol table mustn't include 'gertrude'",
				typer(start).typeExistsFor("gertrude"));
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
		Module module = model.load("");
		assertEquals(model.getTopLevel(), module);

		// We can't reliably specify an exhaustive list so we'll test for some
		// likely suspects.
		String[] likelySuspects = { "abs", "all", "any", "apply", "basestring",
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
				"vars", "xrange", "zip", "Exception", "types" };
		for (String suspect : likelySuspects) {
			assertTrue("Expected symbol '" + suspect
					+ "' not found in top-level.",
					typer(module).typeExistsFor(suspect));
		}
	}
}