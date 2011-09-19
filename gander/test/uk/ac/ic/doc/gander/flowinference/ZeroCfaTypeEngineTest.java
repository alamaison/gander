package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.ScopedAstNode;
import uk.ac.ic.doc.gander.ScopedPrintNode;
import uk.ac.ic.doc.gander.TaggedNodeAndScopeFinder;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TUnion;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.MutableModel;

public class ZeroCfaTypeEngineTest {
	private static final String TEST_FOLDER = "python_test_code/type_engine";

	private MutableModel model;
	private ZeroCfaTypeEngine engine;
	private TClass stringType;
	private TClass integerType;
	private TClass listType;

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
		stringType = new TClass(model.getTopLevel().getClasses().get("str"));
		integerType = new TClass(model.getTopLevel().getClasses().get("int"));
		listType = new TClass(model.getTopLevel().getClasses().get("list"));
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
		ScopedAstNode literal = findNode("literals", literalName.toLowerCase()
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
		ScopedAstNode literal = findNode("literals", literalName.toLowerCase()
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
	public void multipleLocalDefinitions() throws Throwable {
		ScopedPrintNode node = findPrintNode("multiple_local_definitions",
				"am_i_a_string");
		Type type = engine.typeOf(node.getExpression(), node.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		Type expectedType = new TUnion(types);

		assertEquals("Variable's type not inferred correctly. We're "
				+ "expecting a union of str and int because the analysis is "
				+ "flow insensitive", expectedType, type);

		node = findPrintNode("multiple_local_definitions", "am_i_an_integer");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly. We're "
				+ "expecting a union of str and int because the analysis is "
				+ "flow insensitive", expectedType, type);
	}

	/**
	 * A class-scope attribute access from outside the class.
	 */
	@Test
	public void classAttributeOutside() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_attribute_outside",
				"what_am_i");
		Type type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly", stringType, type);
	}

	/**
	 * A class-scope attribute access from inside the class.
	 */
	@Test
	public void classAttributeInside() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_attribute_inside",
				"what_am_i");
		Type type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly", stringType, type);

		node = findPrintNode("class_attribute_inside", "what_am_i_via_self");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly", stringType, type);
	}

	private ScopedAstNode findNode(String moduleName, String tag)
			throws Exception {
		return new TaggedNodeAndScopeFinder(model.loadModule(moduleName), tag)
				.getTaggedNode();
	}

	private ScopedPrintNode findPrintNode(String moduleName, String tag)
			throws Exception {
		return ScopedPrintNode.findPrintNode(model, moduleName, tag);
	}
}
