package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.Print;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ScopedAstNode;
import uk.ac.ic.doc.gander.TaggedNodeAndScopeFinder;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TTop;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public class ZeroCfaTypeEngineTest {

	private static final String TEST_FOLDER = "python_test_code/type_engine";
	private MutableModel model;
	private Hierarchy hierarchy;
	private ZeroCfaTypeEngine engine;

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);
		hierarchy = HierarchyFactory
				.createHierarchy(new File(topLevel.toURI()));
		model = new MutableModel(hierarchy);
		engine = new ZeroCfaTypeEngine(model);
	}

	/**
	 * Tests the literal type inference assuming a convention that the line
	 * containing the expression being typed is tagged with the comment that
	 * contains the name of the literal type, converted to lowercase, followed
	 * by "_literal".
	 * 
	 * @param literalName
	 *            Friendly name of the type of literal.
	 * @param expectedClassName
	 *            Expected name of the inferred class's instance.
	 * @throws Exception
	 */
	private void doListeralTest(String literalName, String expectedClassName)
			throws Throwable {
		Module literals = model.loadModule("literals");
		ScopedAstNode literal = findNode(literals, literalName.toLowerCase()
				+ "_literal");

		Type type = engine.typeOf(((Expr) literal.getNode()).value, literal
				.getScope());

		assertEquals(literalName
				+ " literal's type not inferred as an object type",
				TClass.class, type.getClass());

		assertEquals(literalName + " literal's type not inferred correctly",
				model.getTopLevel().getClasses().get(expectedClassName),
				((TClass) type).getClassInstance());
	}

	/**
	 * Tests the literal type inference across an assignment assuming a
	 * convention that the line containing the expression being typed is tagged
	 * with the comment that contains the name of the literal type, converted to
	 * lowercase, followed by "_literal_assignment".
	 * 
	 * @param literalName
	 *            Friendly name of the type of literal.
	 * @param expectedClassName
	 *            Expected name of the inferred class's instance.
	 * @throws Exception
	 */
	private void doListeralAssignmentTest(String literalName,
			String expectedClassName) throws Throwable {
		Module literals = model.loadModule("literals");
		ScopedAstNode literal = findNode(literals, literalName.toLowerCase()
				+ "_literal_assignment");

		exprType lhs = ((Assign) literal.getNode()).targets[0];
		Type type = engine.typeOf(lhs, literal.getScope());

		assertEquals("Target of " + literalName.toLowerCase()
				+ " literal assignment's type not inferred as "
				+ "an object type", TClass.class, type.getClass());

		assertEquals("Target of " + literalName.toLowerCase()
				+ " literal assignment's type not inferred correctly", model
				.getTopLevel().getClasses().get(expectedClassName),
				((TClass) type).getClassInstance());
	}

	@Test
	public void stringLiteral() throws Throwable {
		doListeralTest("String", "str");
	}

	@Test
	public void dictionaryLiteral() throws Throwable {
		doListeralTest("Dictionary", "dict");
	}

	@Test
	public void listLiteral() throws Throwable {
		doListeralTest("List", "list");
	}

	@Test
	public void integerLiteral() throws Throwable {
		doListeralTest("Integer", "int");
	}

	@Test
	public void longLiteral() throws Throwable {
		doListeralTest("Long", "long");
	}

	@Test
	public void floatLiteral() throws Throwable {
		doListeralTest("Float", "float");
	}

	@Test
	public void tupleLiteral() throws Throwable {
		doListeralTest("Tuple", "tuple");
	}

	@Test
	public void stringLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("String", "str");
	}

	@Test
	public void dictionaryLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("Dictionary", "dict");
	}

	@Test
	public void listLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("List", "list");
	}

	@Test
	public void integerLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("Integer", "int");
	}

	@Test
	public void longLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("Long", "long");
	}

	@Test
	public void floatLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("Float", "float");
	}

	@Test
	public void tupleLiteralAssignment() throws Throwable {
		doListeralAssignmentTest("Tuple", "tuple");
	}

	@Test
	public void globalHiding() throws Throwable {
		Module binding = model.loadModule("binding_global_hiding");
		ScopedAstNode node = findNode(binding, "what_am_i");

		exprType i = ((Expr) node.getNode()).value;
		Type type = engine.typeOf(i, node.getScope());

		assertEquals("Variable i's type not inferred as a class instance",
				TClass.class, type.getClass());

		assertEquals("Variable i's type not inferred correctly. This probably "
				+ "means it picked up the global i instead of the local "
				+ "one bound in the local scope by assignment", model
				.getTopLevel().getClasses().get("str"), ((TClass) type)
				.getClassInstance());
	}

	@Test
	public void subtleGlobalHiding() throws Throwable {
		Module binding = model.loadModule("binding_subtle_global_hiding");
		ScopedAstNode node = findNode(binding, "who_am_i");

		exprType i = ((Print) node.getNode()).values[0];
		Type type = engine.typeOf(i, node.getScope());

		assertEquals("Variable i's type not inferred as Top despite coming "
				+ "from list.  This probably means it picked up the global "
				+ "i instead of the local one declared in the for loop.",
				TTop.class, type.getClass());
	}

	private static ScopedAstNode findNode(Module module, String tag)
			throws Exception {
		ScopedAstNode node = new TaggedNodeAndScopeFinder(module, tag)
				.getTaggedNode();
		assertTrue("Unable to find node tagged with '" + tag + "'",
				node != null);
		return node;
	}
}
