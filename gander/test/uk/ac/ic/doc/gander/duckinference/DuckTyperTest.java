package uk.ac.ic.doc.gander.duckinference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.Statement;
import uk.ac.ic.doc.gander.TaggedBlockFinder;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.OldNamespace;

public class DuckTyperTest {

	private static final String TEST_FOLDER = "python_test_code";
	private MutableModel model;
	private Hierarchy hierarchy;

	public void setup(String caseName) throws Throwable {
		URL testFolder = getClass().getResource(TEST_FOLDER);
		File topLevel = new File(new File(testFolder.toURI()), caseName);

		hierarchy = HierarchyFactory.createHierarchy(topLevel);
		model = new DefaultModel(hierarchy);
	}

	private Set<Type> typeOf(String tag, Function enclosingFunction)
			throws Exception {
		Statement stmt = findCall(enclosingFunction.getCfg(), tag);
		assertTrue("TEST ERROR: tag not found", stmt != null);

		DuckTyper typer = new DuckTyper(model, new TypeResolver(
				new ZeroCfaTypeEngine()));
		Result<Type> type = typer.typeOf(stmt.getCall(), stmt.getBlock(),
				enclosingFunction);
		return type.transformResult(new Transformer<Type, Set<Type>>() {

			@Override
			public Set<Type> transformFiniteResult(Set<Type> result) {
				return result;
			}

			@Override
			public Set<Type> transformInfiniteResult() {
				throw new AssertionError("This code wasn't "
						+ "written to cope with an "
						+ "infinite type.  Update it.");
			}
		});
	}

	private Statement findCall(Cfg graph, String tag) throws Exception {
		return new TaggedBlockFinder(graph).findTaggedStatement(tag);
	}

	@Test
	public void infileSingle() throws Throwable {
		setup("infile_single");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));
	}

	@Test
	public void infileAmbiguous() throws Throwable {
		setup("infile_ambiguous");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")), new TObject(start
						.getClasses().get("B")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")), new TObject(start
						.getClasses().get("B")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")), new TObject(start
						.getClasses().get("B")));
	}

	@Test
	public void sibling() throws Throwable {
		setup("sibling");

		Module start = model.loadModule("start");
		Module sibling = model.lookup("sibling");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TObject(sibling.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TObject(sibling.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TObject(sibling.getClasses().get("A")));
	}

	@Test
	public void siblingSourceAndDef() throws Throwable {
		setup("sibling_source_and_def");

		Module start = model.loadModule("start");
		Module sibling = model.lookup("sibling");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TObject(sibling.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TObject(sibling.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TObject(sibling.getClasses().get("A")));
	}

	@Test
	public void conditionalType() throws Throwable {
		setup("conditional_type");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")), new TObject(start
						.getClasses().get("B")), new TObject(start.getClasses()
						.get("C")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")), new TObject(start
						.getClasses().get("B")), new TObject(start.getClasses()
						.get("C")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));
	}

	@Test
	public void inherited() throws Throwable {
		setup("inherited");

		Module start = model.loadModule("start");

		assertInference("x.a(tag1)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));

		assertInference("x.b(tag2)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));

		assertInference("x.c(tag3)", start.getFunctions().get("main"),
				new TObject(start.getClasses().get("A")));
	}

	private void assertInference(String tag, OldNamespace scope,
			Type... expected) throws Exception {
		Set<Type> type = typeOf(tag, (Function) scope);

		assertEquals("'" + tag
				+ "' was inferred to an unexpected number of types",
				expected.length, type.size());

		assertTrue("Not all inferred concrete types were expected; inferred: "
				+ type + " expected: " + Arrays.asList(expected), Arrays
				.asList(expected).containsAll(type));
	}

}
