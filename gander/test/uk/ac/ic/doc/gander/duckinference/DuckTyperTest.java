package uk.ac.ic.doc.gander.duckinference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.Statement;
import uk.ac.ic.doc.gander.TaggedBlockFinder;
import uk.ac.ic.doc.gander.TestHierarchyBuilder;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

public class DuckTyperTest {

	private static final String TEST_FOLDER = "python_test_code";
	private Model model;
	private Hierarchy hierarchy;

	public void setup(String caseName) throws Throwable {
		URL testFolder = getClass().getResource(TEST_FOLDER);
		File topLevel = new File(new File(testFolder.toURI()), caseName);

		hierarchy = TestHierarchyBuilder.createHierarchy(topLevel);
		model = new Model(hierarchy);
	}

	private Set<Type> typeOf(String tag, Function enclosingFunction)
			throws Exception {
		Statement stmt = findCall(enclosingFunction.getCfg(), tag);
		assertTrue("TEST ERROR: tag not found", stmt != null);

		DuckTyper typer = new DuckTyper(model);
		return typer.typeOf(stmt.getCall(), stmt.getBlock(), enclosingFunction);
	}

	private Statement findCall(Cfg graph, String tag) throws Exception {
		return new TaggedBlockFinder(graph).findTaggedStatement(tag);
	}

	@Test
	public void infileSingle() throws Throwable {
		setup("infile_single");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")));
	}

	@Test
	public void infileAmbiguous() throws Throwable {
		setup("infile_ambiguous");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")), new TClass(start
						.getClasses().get("B")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")), new TClass(start
						.getClasses().get("B")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")), new TClass(start
						.getClasses().get("B")));
	}

	@Test
	public void sibling() throws Throwable {
		setup("sibling");

		Module start = model.loadModule("start");
		Module sibling = model.lookupModule("sibling");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TClass(sibling.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TClass(sibling.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TClass(sibling.getClasses().get("A")));
	}

	@Test
	public void siblingSourceAndDef() throws Throwable {
		setup("sibling_source_and_def");

		Module start = model.loadModule("start");
		Module sibling = model.lookupModule("sibling");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TClass(sibling.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TClass(sibling.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TClass(sibling.getClasses().get("A")));
	}

	@Test
	public void conditionalType() throws Throwable {
		setup("conditional_type");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")), new TClass(start
						.getClasses().get("B")), new TClass(start.getClasses()
						.get("C")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")), new TClass(start
						.getClasses().get("B")), new TClass(start.getClasses()
						.get("C")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TClass(start.getClasses().get("A")));
	}

	private void assertInference(String tag, Namespace scope, Type... expected)
			throws Exception {
		Set<Type> type = typeOf(tag, (Function) scope);

		assertEquals("'" + tag
				+ "' was inferred to an unexpected number of types",
				expected.length, type.size());

		for (Type t : type) {
			// FIXME: This is wrong. Should be of type object (instance of
			// class)
			// not a class itself.

			assertTrue("'" + tag + "' was not inferred to a class type",
					t instanceof TClass);

			Class c = ((TClass) t).getClassInstance();
			assertTrue("'" + tag + "' was inferred to a class "
					+ "but not an expected one: " + c, isClassInTypeSet(
					expected, c));
		}
	}

	private boolean isClassInTypeSet(Type expected[], Class klass) {
		for (Type e : expected) {
			if (((TClass) e).getClassInstance().equals(klass))
				return true;
		}

		return false;
	}

}
