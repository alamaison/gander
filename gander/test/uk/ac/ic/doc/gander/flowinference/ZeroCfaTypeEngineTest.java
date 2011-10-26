package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.ScopedAstNode;
import uk.ac.ic.doc.gander.ScopedPrintNode;
import uk.ac.ic.doc.gander.TaggedNodeAndScopeFinder;
import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.ModelCodeBlockWalker;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Namespace;

public class ZeroCfaTypeEngineTest {
	private static final String TEST_FOLDER = "python_test_code/type_engine";

	private MutableModel model;
	private TypeEngine engine;
	private TObject stringType;
	private TObject integerType;
	private TObject listType;
	private TClass noneType;

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
		stringType = new TObject(model.getTopLevel().getClasses().get("str"));
		integerType = new TObject(model.getTopLevel().getClasses().get("int"));
		listType = new TObject(model.getTopLevel().getClasses().get("list"));
		// noneType = new TClass(model.getTopLevel().getModules().get("types")
		// .getClasses().get("NoneType"));
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

		TypeJudgement type = engine.typeOf(((Expr) literal.getNode()).value,
				literal.getScope());

		Type expectedType = new TObject(model.getTopLevel().getClasses().get(
				expectedClassName));

		assertEquals("Target of " + literalName.toLowerCase()
				+ " literal's type not inferred correctly",
				new SetBasedTypeJudgement(expectedType), type);
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
		TypeJudgement type = engine.typeOf(lhs, literal.getScope());

		Type expectedType = new TObject(model.getTopLevel().getClasses().get(
				expectedClassName));

		assertEquals("Target of " + literalName.toLowerCase()
				+ " literal assignment's type not inferred correctly",
				new SetBasedTypeJudgement(expectedType), type);
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
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

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
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(stringType), type);
	}

	/**
	 * A class-scope attribute access from inside the class.
	 */
	@Test
	public void classAttributeInside() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_attribute_inside",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(stringType), type);

		node = findPrintNode("class_attribute_inside", "what_am_i_via_self");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(stringType), type);
	}

	/**
	 * A variable is assigned directly to itself. This might cause type
	 * inference to recurse infinitely leading to a stack overflow unless
	 * handled properly.
	 */
	@Test
	public void selfAssignment() throws Throwable {
		ScopedPrintNode node = findPrintNode("self_assignment", "what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(stringType), type);
	}

	/**
	 * Like selfAssignment, a variable is assigned to itself but, this time,
	 * indirectly. The consequences are the same but harder to detect the case
	 * and prevent it.
	 */
	@Test
	public void selfAssignmentIndirect() throws Throwable {
		ScopedPrintNode node = findPrintNode("self_assignment_indirect",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(stringType), type);
	}

	/**
	 * Infer a type based on assignment from a constructor.
	 */
	@Test
	public void constructor() throws Throwable {
		ScopedPrintNode node = findPrintNode("constructor", "what_is_a");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(new TObject(node.getGlobalNamespace()
						.getClasses().get("A"))), type);

		node = findPrintNode("constructor", "what_is_b");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(new TObject(node.getGlobalNamespace()
						.getClasses().get("A"))), type);
	}

	/**
	 * Type of constructor return value and instance return value should be
	 * different.
	 */
	@Test
	public void constructorVsInstance() throws Throwable {
		ScopedPrintNode node = findPrintNode("constructor_vs_instance",
				"what_is_a");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Variable's type not inferred correctly",
				new SetBasedTypeJudgement(new TObject(node.getGlobalNamespace()
						.getClasses().get("A"))), type);

		node = findPrintNode("constructor_vs_instance", "what_is_b");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly. This probably "
				+ "means it gave the type of a class instance the type "
				+ "of the class object (metaclass)", Top.INSTANCE, type);
	}

	/**
	 * The inferred type of the class but be different from the inferred type of
	 * its instances.
	 */
	@Test
	public void metaclass() throws Throwable {
		ScopedPrintNode node = findPrintNode("metaclass", "am_i_a_class");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("W not inferred as a metaclass",
				new SetBasedTypeJudgement(new TClass(node.getGlobalNamespace()
						.getClasses().get("W"))), type);

		node = findPrintNode("metaclass", "am_i_also_a_class");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("W not inferred as a metaclass",
				new SetBasedTypeJudgement(new TClass(node.getGlobalNamespace()
						.getClasses().get("W"))), type);

		node = findPrintNode("metaclass", "am_i_an_instance");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly. This probably "
				+ "means it gave the type of a class instance the type "
				+ "of the class object (metaclass)", new SetBasedTypeJudgement(
				new TObject(node.getGlobalNamespace().getClasses().get("W"))),
				type);

		node = findPrintNode("metaclass", "am_i_also_an_instance");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Variable's type not inferred correctly. This probably "
				+ "means it gave the type of a class instance the type "
				+ "of the class object (metaclass)", new SetBasedTypeJudgement(
				new TObject(node.getGlobalNamespace().getClasses().get("W"))),
				type);
	}

	/**
	 * Resolve function name.
	 */
	@Test
	public void function() throws Throwable {
		ScopedPrintNode node = findPrintNode("function", "what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());
		TypeJudgement expectedType = new SetBasedTypeJudgement(new TFunction(
				node.getGlobalNamespace().getFunctions().get("f")));

		assertEquals("Function type not inferred correctly", expectedType, type);
	}

	/**
	 * Infer return type of a function taking no arguments and returning a
	 * monomorph.
	 */
	@Test
	public void functionReturnNullaryMono() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return_nullary_mono",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());
		TypeJudgement expectedType = new SetBasedTypeJudgement(integerType);

		assertEquals("Function return's type not inferred correctly",
				expectedType, type);
	}

	/**
	 * Infer return type of a function taking no arguments and returning a
	 * polymorph.
	 * 
	 * Flow/context insensitive analysis will infer the return type as a union
	 * of int and X even though the latter is unreachable.
	 */
	@Test
	public void functionReturnNullaryPoly() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return_nullary_poly",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(integerType);
		types.add(new TObject(node.getGlobalNamespace().getFunctions().get("f")
				.getClasses().get("X")));
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		assertEquals("Function return's type not inferred correctly",
				expectedType, type);
	}

	/**
	 * Infer return type of a function taking no arguments and returning a
	 * polymorph via a nested call.
	 */
	@Test
	public void functionReturnNullaryChained() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return_nullary_chained",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		types.add(new TObject(node.getGlobalNamespace().getFunctions().get("f")
				.getClasses().get("X")));
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		assertEquals("Function return's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionReturn() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return", "what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Inferred type should be monomorphic", 1,
				((SetBasedTypeJudgement) type).getConstituentTypes().size());
		assertEquals("Function return's type not inferred correctly",
				"NoneType", ((TObject) ((SetBasedTypeJudgement) type)
						.getConstituentTypes().iterator().next()).getName());
	}

	@Test
	public void functionReturnMissing() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return_missing",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Function return's type not inferred correctly", noneType,
				((TObject) ((SetBasedTypeJudgement) type).getConstituentTypes()
						.iterator().next()).getClassInstance());
	}

	@Test
	public void methodReturn() throws Throwable {
		ScopedPrintNode node = findPrintNode("method_return", "what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Inferred type should be monomorphic",
				new SetBasedTypeJudgement(listType), type);
	}

	private void doGlobalTest(String tag) throws Exception {
		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		ScopedPrintNode node = findPrintNode("global", tag);
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Global's type not inferred correctly.", expectedType,
				type);

	}

	private void doGlobalDefinedInOtherModuleTest(String moduleName, String tag)
			throws Exception {
		ScopedPrintNode node = findPrintNode(moduleName, tag);

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		types.add(listType);
		types.add(new TObject(model.loadModule(
				"global_defined_in_other_module_worker").getClasses()
				.get("Bob")));
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Global's type not inferred correctly.", expectedType,
				type);
	}

	@Test
	public void global() throws Throwable {
		doGlobalTest("what_am_i_without_global_statement");
		doGlobalTest("what_am_i_with_global_statement");
		doGlobalTest("what_am_i_at_global_scope");
	}

	@Test
	public void globalDefinedInOtherModule() throws Throwable {
		doGlobalDefinedInOtherModuleTest("global_defined_in_other_module",
				"what_am_i_without_global_statement");
		doGlobalDefinedInOtherModuleTest("global_defined_in_other_module",
				"what_am_i_with_global_statement");
		doGlobalDefinedInOtherModuleTest("global_defined_in_other_module",
				"what_am_i_at_global_scope");
		doGlobalDefinedInOtherModuleTest(
				"global_defined_in_other_module_worker",
				"what_am_i_without_global_statement_in_foreign_module");
		doGlobalDefinedInOtherModuleTest(
				"global_defined_in_other_module_worker",
				"what_am_i_with_global_statement_in_foreign_module");
		doGlobalDefinedInOtherModuleTest(
				"global_defined_in_other_module_worker",
				"what_am_i_at_global_scope_in_foreign_module");
	}

	@Test
	public void methodParameterSelf() throws Throwable {
		ScopedPrintNode node = findPrintNode("method_parameter_self",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());
		TypeJudgement expectedType = new SetBasedTypeJudgement(new TObject(node
				.getGlobalNamespace().getClasses().get("A")));

		assertEquals("Function parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionParameterMono() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_parameter_mono",
				"what_am_i");

		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());
		TypeJudgement expectedType = new SetBasedTypeJudgement(stringType);

		assertEquals("Function parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionParameterPoly() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_parameter_poly",
				"what_am_i");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		assertEquals("Function parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void objectAttributeMono() throws Throwable {
		String testName = "object_attribute_mono";
		TypeJudgement expectedType = new SetBasedTypeJudgement(listType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_inside");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "what_am_i_outside");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributePoly() throws Throwable {
		String testName = "object_attribute_poly";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_inside");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(integerType);
		types.add(listType);
		types.add(new TObject(node.getGlobalNamespace().getClasses().get("A")));
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "what_am_i_outside");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributeClassDictFallbackInitDel() throws Throwable {
		String testName = "object_attribute_class_dict_fallback_init_del";

		ScopedPrintNode node = findPrintNode(testName,
				"i_am_really_the_instance_var");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(listType);
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "i_am_really_the_class_var");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributeClassDictFallbackWrite() throws Throwable {
		String testName = "object_attribute_class_dict_fallback_write";

		ScopedPrintNode node = findPrintNode(testName,
				"i_am_really_the_instance_var");
		TypeJudgement type = engine.typeOf(node.getExpression(), node
				.getScope());

		ArrayList<Type> types = new ArrayList<Type>();
		types.add(stringType);
		types.add(listType);
		TypeJudgement expectedType = new SetBasedTypeJudgement(types);

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "i_am_really_the_class_var");
		type = engine.typeOf(node.getExpression(), node.getScope());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
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
