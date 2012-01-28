package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.ScopedAstNode;
import uk.ac.ic.doc.gander.ScopedPrintNode;
import uk.ac.ic.doc.gander.TaggedNodeAndScopeFinder;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.TBoundMethod;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.ImportPath;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObjects;

public class ZeroCfaTypeEngineTest {
	private static final String TEST_FOLDER = "python_test_code/type_engine";

	private MutableModel model;
	private TypeEngine engine;
	private Type stringType;
	private Type integerType;
	private Type listType;
	private Type noneType;
	private Type tupleType;
	private Type dictType;

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
		stringType = new TObject(builtinClass("str"));
		integerType = new TObject(builtinClass("int"));
		listType = new TObject(builtinClass("list"));
		tupleType = new TObject(builtinClass("tuple"));
		dictType = new TObject(builtinClass("dict"));
		noneType = new TObject(builtinClass("__BuiltinNoneType__"));
		engine = new ZeroCfaTypeEngine();
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

		Result<Type> type = engine.typeOf(new ModelSite<exprType>(
				((Expr) literal.getNode()).value, literal.getScope()));

		Type expectedType = new TObject(builtinClass(expectedClassName));

		assertEquals("Target of " + literalName.toLowerCase()
				+ " literal's type not inferred correctly",
				typeJudgement(expectedType), type);
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
		Result<Type> type = engine.typeOf(new ModelSite<exprType>(lhs, literal
				.getScope()));

		Set<Type> expectedType = Collections.<Type> singleton(new TObject(
				builtinClass(expectedClassName)));

		assertEquals("Target of " + literalName.toLowerCase()
				+ " literal assignment's type not inferred correctly",
				expectedType, type);
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
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Variable's type not inferred correctly. We're "
				+ "expecting a union of str and int because the analysis is "
				+ "flow insensitive", expectedType, type);

		node = findPrintNode("multiple_local_definitions", "am_i_an_integer");
		type = engine.typeOf(node.site());

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
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(stringType), type);
	}

	/**
	 * A class-scope attribute access from inside the class.
	 */
	@Test
	public void classAttributeInside() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_attribute_inside",
				"what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(stringType), type);

		node = findPrintNode("class_attribute_inside", "what_am_i_via_self");
		type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(stringType), type);
	}

	/**
	 * Class scope attribute whose only definition appears outside the class
	 * body.
	 */
	@Test
	public void classAttributeAssignedOutside() throws Throwable {
		String testName = "class_attribute_assigned_outside";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly "
				+ "when accessed outside the scope of the namespace.",
				typeJudgement(stringType), type);

		node = findPrintNode(testName, "what_am_i_via_self");
		type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly "
				+ "when accessed via the object instance's 'self'.",
				typeJudgement(stringType), type);

		node = findPrintNode(testName, "what_am_i_inside");
		type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly. This "
				+ "probably means the analysis didn't realise it "
				+ "binds globally as there is no local definition "
				+ "before it is 'executed'", typeJudgement(integerType), type);
	}

	/**
	 * A variable is assigned directly to itself. This might cause type
	 * inference to recurse infinitely leading to a stack overflow unless
	 * handled properly.
	 */
	@Test
	public void selfAssignment() throws Throwable {
		ScopedPrintNode node = findPrintNode("self_assignment", "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(stringType), type);
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
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(stringType), type);
	}

	/**
	 * Infer a type based on assignment from a constructor.
	 */
	@Test
	public void constructorResult() throws Throwable {
		String testName = "constructor_result";
		ScopedPrintNode node = findPrintNode(testName, "what_is_a");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(new TObject(moduleLevelClass(node, "A"))), type);

		node = findPrintNode(testName, "what_is_b");
		type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(new TObject(moduleLevelClass(node, "A"))), type);
	}

	/**
	 * Type of constructor return value and instance return value should be
	 * different.
	 */
	@Test
	public void constructorVsInstance() throws Throwable {
		ScopedPrintNode node = findPrintNode("constructor_vs_instance",
				"what_is_a");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly",
				typeJudgement(new TObject(moduleLevelClass(node, "A"))), type);

		node = findPrintNode("constructor_vs_instance", "what_is_b");
		type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly. This probably "
				+ "means it gave the type of a class instance the type "
				+ "of the class object (metaclass)", TopT.INSTANCE, type);
	}

	/**
	 * The inferred type of the class but be different from the inferred type of
	 * its instances.
	 */
	@Test
	public void metaclass() throws Throwable {
		ScopedPrintNode node = findPrintNode("metaclass", "am_i_a_class");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("W not inferred as a metaclass", typeJudgement(new TClass(
				moduleLevelClass(node, "W"))), type);

		node = findPrintNode("metaclass", "am_i_also_a_class");
		type = engine.typeOf(node.site());

		assertEquals("W not inferred as a metaclass", typeJudgement(new TClass(
				moduleLevelClass(node, "W"))), type);

		node = findPrintNode("metaclass", "am_i_an_instance");
		type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly. This probably "
				+ "means it gave the type of a class instance the type "
				+ "of the class object (metaclass)", typeJudgement(new TObject(
				moduleLevelClass(node, "W"))), type);

		node = findPrintNode("metaclass", "am_i_also_an_instance");
		type = engine.typeOf(node.site());

		assertEquals("Variable's type not inferred correctly. This probably "
				+ "means it gave the type of a class instance the type "
				+ "of the class object (metaclass)", typeJudgement(new TObject(
				moduleLevelClass(node, "W"))), type);
	}

	/**
	 * Resolve function name.
	 */
	@Test
	public void function() throws Throwable {
		ScopedPrintNode node = findPrintNode("function", "what_am_i");
		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(new TFunction(
				moduleLevelFunction(node, "f")));

		assertEquals("Function type not inferred correctly", expectedType, type);
	}

	/**
	 * Resolve method.
	 */
	@Test
	public void method() throws Throwable {
		String testName = "method";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i_via_class");
		Result<Type> type = engine.typeOf(node.site());

		ClassCO klass = moduleLevelClass(node, "A");
		FunctionCO unboundMethod = nestedFunction(klass, "method");

		Set<Type> expectedBoundType = typeJudgement(new TBoundMethod(
				unboundMethod, new TObject(klass)));
		Set<Type> expectedUnboundType = typeJudgement(new TFunction(
				unboundMethod));

		assertEquals("Method not resolved correctly via class",
				expectedUnboundType, type);

		node = findPrintNode(testName, "what_am_i_via_instance");
		type = engine.typeOf(node.site());

		assertEquals("Method not resolved correctly via instance",
				expectedBoundType, type);
	}

	/**
	 * Resolve method.
	 */
	@Test
	public void method2() throws Throwable {
		String testName = "method2";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		ClassCO klass = moduleLevelClass(node, "A");
		FunctionCO unboundMethod = nestedFunction(klass, "g");
		Set<Type> expectedType = typeJudgement(new TBoundMethod(unboundMethod,
				new TObject(klass)));

		assertEquals("Method not resolved correctly", expectedType, type);
	}

	/**
	 * Infer return type of a function taking no arguments and returning a
	 * monomorph.
	 */
	@Test
	public void functionReturnNullaryMono() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return_nullary_mono",
				"what_am_i");
		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(integerType);

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
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(integerType);
		expectedType.add(new TObject(nestedClass(
				moduleLevelFunction(node, "f"), "X")));

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
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);
		expectedType.add(new TObject(nestedClass(
				moduleLevelFunction(node, "f"), "X")));

		assertEquals("Function return's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionReturn() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return", "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Inferred type should be monomorphic", 1,
				((FiniteResult<Type>) type).size());
		assertEquals("Function return's type not inferred correctly", noneType,
				(TObject) ((FiniteResult<Type>) type).iterator().next());
	}

	@Test
	public void functionReturnMissing() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_return_missing",
				"what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Function return's type not inferred correctly", noneType,
				(TObject) ((FiniteResult<Type>) type).iterator().next());
	}

	@Test
	public void methodReturn() throws Throwable {
		ScopedPrintNode node = findPrintNode("method_return", "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Inferred type should be monomorphic",
				typeJudgement(listType), type);
	}

	private void doGlobalTest(String testName, String tag) throws Exception {
		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		ScopedPrintNode node = findPrintNode(testName, tag);
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Global's type not inferred correctly.", expectedType,
				type);
	}

	@Test
	public void global() throws Throwable {
		doGlobalTest("global", "what_am_i_without_global_statement");
		doGlobalTest("global", "what_am_i_with_global_statement");
		doGlobalTest("global", "what_am_i_at_global_scope");
	}

	@Test
	public void globalImported() throws Throwable {
		doGlobalTest("global_imported", "what_am_i");
	}

	@Test
	public void globalImportedFrom() throws Throwable {
		doGlobalTest("global_imported_from",
				"what_am_i_without_global_statement");
		doGlobalTest("global_imported_from", "what_am_i_with_global_statement");
		doGlobalTest("global_imported_from", "what_am_i_at_global_scope");
	}

	@Test
	public void globalRedefinedInOtherModule() throws Throwable {
		doGlobalTest("global_redefined_in_other_module",
				"what_am_i_before_import");
		doGlobalTest("global_redefined_in_other_module",
				"what_am_i_after_import");
	}

	@Test
	public void methodParameterMono() throws Throwable {
		String testName = "method_parameter_mono";
		Set<Type> expectedType = typeJudgement(integerType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Method parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void methodParameterPoly() throws Throwable {
		String testName = "method_parameter_poly";
		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);
		expectedType.add(listType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Method parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void methodParameterDefault() throws Throwable {
		String testName = "method_parameter_default";
		Set<Type> expectedType = typeJudgement(stringType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Method's default parameter type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void methodParameterBound() throws Throwable {
		String testName = "method_parameter_bound";
		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Method parameter's type not inferred correctly. This "
				+ "probably means the analysis failed to "
				+ "realise the bound method called as a closure calls A.m.",
				expectedType, type);
	}

	@Test
	public void methodParameterSelf() throws Throwable {
		String testName = "method_parameter_self";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(new TObject(moduleLevelClass(
				node, "A")));

		assertEquals("Method parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void methodParameterAssigned() throws Throwable {
		String testName = "method_parameter_assigned";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(integerType);

		assertEquals("Method parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void methodParameterSelfAssigned() throws Throwable {
		String testName = "method_parameter_self_assigned";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(new TObject(moduleLevelClass(
				node, "A")));

		assertEquals("Method parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void methodParameterInherited() throws Throwable {
		String testName = "method_parameter_inherited";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(dictType);
		expectedType.add(stringType);

		assertEquals("'Parameter type not inferred correctly. "
				+ "This probably means it failed to realise that 'm' is "
				+ "called from a subclass instance.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedAssigned1() throws Throwable {
		String testName = "method_parameter_inherited_assigned1";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(listType);

		assertEquals("Parameter type not inferred correctly. "
				+ "This probably means it failed to realise that 'm' is "
				+ "called from a subclass instance.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedAssigned2() throws Throwable {
		String testName = "method_parameter_inherited_assigned2";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(listType);

		assertEquals("Parameter type not inferred correctly. "
				+ "This probably means it failed to realise that 'm' is "
				+ "called from a subclass instance.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedSelf() throws Throwable {
		String testName = "method_parameter_inherited_self";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(new TObject(moduleLevelClass(node, "A")));
		expectedType.add(new TObject(moduleLevelClass(node, "B")));

		assertEquals("'self' parameter type not inferred correctly. "
				+ "This probably means it failed to realise that 'm' is "
				+ "called from a subclass instance so self also has the "
				+ "subclass type.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedSelfAssigned1() throws Throwable {
		String testName = "method_parameter_inherited_self_assigned1";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(new TObject(moduleLevelClass(node, "B")));

		assertEquals("'self' parameter type not inferred correctly. "
				+ "This probably means it failed to realise that 'm' is "
				+ "called from a subclass instance so self also has the "
				+ "subclass type.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedSelfAssigned2() throws Throwable {
		String testName = "method_parameter_inherited_self_assigned2";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(new TObject(moduleLevelClass(node, "B")));

		assertEquals("'self' parameter type not inferred correctly. "
				+ "This probably means it failed to realise that 'm' is "
				+ "called from a subclass instance so self also has the "
				+ "subclass type.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedAbstract() throws Throwable {
		String testName = "method_parameter_inherited_abstract";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(listType);

		assertEquals("Oarameter type not inferred correctly. "
				+ "This probably means it failed to realise that the super "
				+ "class is never instantiated and 'm' is only "
				+ "called from a subclass instance.", expectedType, type);
	}

	@Test
	public void methodParameterInheritedAbstractSelf() throws Throwable {
		String testName = "method_parameter_inherited_abstract_self";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(new TObject(moduleLevelClass(node, "Concrete")));

		assertEquals("'self' parameter type not inferred correctly. "
				+ "This probably means it failed to realise that the super "
				+ "class is never instantiated and 'm' is only "
				+ "called from a subclass instance so self only has the "
				+ "subclass type.", expectedType, type);
	}

	@Test
	public void methodParameterCalledExplicitly() throws Throwable {
		ScopedPrintNode node = findPrintNode(
				"method_parameter_called_explicitly", "what_am_i");
		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(listType);

		assertEquals("Method parameter type not inferred correctly. "
				+ "This probably means it failed to realise that calling the "
				+ "method on the class and explicitly passing an instance "
				+ "has the same effect as calling the method on the "
				+ "instance directly", expectedType, type);
	}

	@Test
	public void constructorParameter() throws Throwable {
		String testName = "constructor_parameter";
		Set<Type> expectedType = typeJudgement(integerType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Constructor parameter's type not inferred correctly. "
				+ "This probably means it didn't treat the "
				+ "constructor properly and just tried looking for "
				+ "explicit calls to __init__.", expectedType, type);
	}

	@Test
	public void constructorParameterExplicit() throws Throwable {
		String testName = "constructor_parameter_explicit";
		Set<Type> expectedType = typeJudgement(integerType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Constructor parameter's type not inferred correctly "
				+ "when called explictly. Maybe it only treated the "
				+ "constructor specially and forgot to look for "
				+ "explicit calls to __init__.", expectedType, type);
	}

	@Test
	public void constructorParameterSelf() throws Throwable {
		String testName = "constructor_parameter_self";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(new TObject(moduleLevelClass(
				node, "A")));

		assertEquals("Constructor parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void constructorParameterSelfAssigned() throws Throwable {
		String testName = "constructor_parameter_self_assigned";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(new TObject(moduleLevelClass(
				node, "A")));

		assertEquals("Constructor parameter's type not inferred "
				+ "correctly. The analysis probably missed the "
				+ "constructor being declared outside the "
				+ "class and inserted by assignment.", expectedType, type);
	}

	@Test
	public void constructorParameterInherited() throws Throwable {
		String testName = "constructor_parameter_inherited";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(tupleType);

		assertEquals("Parameter type not inferred correctly. "
				+ "This probably means it failed to realise that the "
				+ "constructor is called from the subclass.", expectedType,
				type);
	}

	@Test
	public void constructorParameterInheritedSelf() throws Throwable {
		String testName = "constructor_parameter_inherited_self";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");

		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(new TObject(moduleLevelClass(node, "S")));

		assertEquals("'self' parameter type not inferred correctly. "
				+ "This probably means it failed to realise that the "
				+ "constructor is called from the subclass.", expectedType,
				type);
	}

	@Test
	public void functionParameterMono() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_parameter_mono",
				"what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(stringType);

		assertEquals("Function parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionParameterPoly() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_parameter_poly",
				"what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionParameterMonoPolyCall() throws Throwable {
		TestModule test = newTestModule("function_parameter_mono_poly_call");

		Result<Type> typeX = engine
				.typeOf(test.printNode("what_am_i_x").site());

		Set<Type> expectedTypeX = Collections.singleton(stringType);

		assertEquals("Function parameter's type not inferred correctly. "
				+ "This probably means the analysis saw that g can be "
				+ "called with two types and got confused that, because "
				+ "it shares a callsite with f, f must have two parameter "
				+ "types too.", expectedTypeX, typeX);

		Result<Type> typeY = engine
				.typeOf(test.printNode("what_am_i_y").site());

		Set<Type> expectedTypeY = Collections.singleton(listType);

		assertEquals("Function parameter's type not inferred correctly. "
				+ "This probably means the analysis saw that g can be "
				+ "called with two types and got confused that, because "
				+ "it shares a callsite with f, f must have two parameter "
				+ "types too.", expectedTypeY, typeY);
	}

	@Test
	public void functionParameterMonoPolyCallMixed() throws Throwable {
		TestModule test = newTestModule("function_parameter_mono_poly_call_mixed");

		Result<Type> typeX = engine
				.typeOf(test.printNode("what_am_i_x").site());

		Set<Type> expectedTypeX = Collections.singleton(stringType);

		assertEquals("Function parameter's type not inferred correctly. "
				+ "This probably means the analysis saw that g can be "
				+ "called with two types and got confused that, because "
				+ "it shares a callsite with f, f must have two parameter "
				+ "types too.", expectedTypeX, typeX);

		Result<Type> typeY = engine
				.typeOf(test.printNode("what_am_i_y").site());

		Set<Type> expectedTypeY = Collections.singleton(listType);

		assertEquals("Function parameter's type not inferred correctly. "
				+ "This probably means the analysis saw that g can be "
				+ "called with two types and got confused that, because "
				+ "it shares a callsite with f, f must have two parameter "
				+ "types too.", expectedTypeY, typeY);
	}

	@Test
	public void functionParameterDefault() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_parameter_default",
				"what_am_i");

		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = typeJudgement(stringType);

		assertEquals("Function parameter's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void functionParameterCalledWithDifferentName() throws Throwable {
		String testName = "function_parameter_called_with_different_name";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function had flowed to g and was called "
				+ "from there.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModule() throws Throwable {
		String testName = "function_parameter_called_from_other_module";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function is imported into another "
				+ "module and called from there with a different type "
				+ "of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleWithDifferentName()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_with_different_name";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function is imported into another "
				+ "module and called from there with a different type "
				+ "of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleViaFrom()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_via_from";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function is imported into another "
				+ "module and called from there with a different type "
				+ "of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleViaFromWithDifferentName()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_via_from_with_different_name";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function is imported into another "
				+ "module and called from there with a different type "
				+ "of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleViaFrommedParent()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_via_frommed_parent";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function's container is imported into another "
				+ "module which allows it to be called from that container's "
				+ "import name by attribute access.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleThroughCall()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_through_call";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is called via an import indirected "
				+ "through a function call and called from there with "
				+ "a different type of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleThroughClass()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_through_class";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		ClassCO x = nestedClass(
				model.lookup(ImportPath
						.fromDottedName("function_parameter_called_from_other_module_through_class_aux")),
				"X");
		expectedType.add(new TObject(x));

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is called via an import indirected "
				+ "through a class member and called from there with "
				+ "a different type of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleThroughGlobal()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_through_global";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is called via an imported "
				+ "global with a different type of parameter.", expectedType,
				type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleThroughGlobalViaFrom()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_through_global_via_from";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is called via an imported "
				+ "global with a different type of parameter.", expectedType,
				type);
	}

	@Test
	public void functionParameterCalledFromOtherModuleViaAlias()
			throws Throwable {
		String testName = "function_parameter_called_from_other_module_via_alias";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is called in another module whose "
				+ "import name is first aliased.", expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughCall() throws Throwable {
		String testName = "function_parameter_called_through_call";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);
		expectedType.add(listType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is indirected "
				+ "through a call whose result is called again with "
				+ "a different type of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughCallParameter() throws Throwable {
		String testName = "function_parameter_called_through_call_parameter";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is passed into another function "
				+ "and called from there.", expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughList() throws Throwable {
		String testName = "function_parameter_called_through_list";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "do the honourable thing when it lost track of the function "
				+ "object's flow.  It should have said I don't know (TopT).",
				TopT.INSTANCE, type);
	}

	@Test
	public void functionParameterCalledThroughDict() throws Throwable {
		String testName = "function_parameter_called_through_dict";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "do the honourable thing when it lost track of the function "
				+ "object's flow.  It should have said I don't know (TopT).",
				TopT.INSTANCE, type);
	}

	@Test
	public void functionParameterCalledThroughObject1() throws Throwable {
		String testName = "function_parameter_called_through_object1";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "follow the flow of the function via an object's attribute.",
				expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughObject2() throws Throwable {
		String testName = "function_parameter_called_through_object2";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "follow the flow of the function via an object's attribute.",
				expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughClass() throws Throwable {
		String testName = "function_parameter_called_through_class";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(new TObject(moduleLevelClass(node, "X")));

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is indirected "
				+ "through a class member and called from there with "
				+ "a different type of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughClassInMethod() throws Throwable {
		String testName = "function_parameter_called_through_class_in_method";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(new TObject(moduleLevelClass(node, "X")));

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is indirected "
				+ "through a class member and called from there with "
				+ "a different type of parameter.", expectedType, type);
	}

	@Test
	public void functionParameterCalledThroughGlobal() throws Throwable {
		String testName = "function_parameter_called_through_global";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "see that the function is indirected "
				+ "through a global alias and called with that name and "
				+ "a different type of parameter.", expectedType, type);
	}

	@Test
	public void objectAttributeMono() throws Throwable {
		String testName = "object_attribute_mono";
		Set<Type> expectedType = typeJudgement(listType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_inside");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "what_am_i_outside");
		type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributePoly() throws Throwable {
		String testName = "object_attribute_poly";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_inside");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(integerType);
		expectedType.add(listType);
		expectedType.add(new TObject(moduleLevelClass(node, "A")));

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "what_am_i_outside");
		type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributeDistracted() throws Throwable {
		String testName = "object_attribute_distracted";
		Set<Type> expectedType = typeJudgement(listType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_inside");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly. "
				+ "This probably means it was distracted by the "
				+ "attribute with the same name in the unrelated class.",
				expectedType, type);

		node = findPrintNode(testName, "what_am_i_outside");
		type = engine.typeOf(node.site());

		expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Attribute's type not inferred correctly. "
				+ "It should include both possibilities for i as it can't "
				+ "statically determine if the object is A or B", expectedType,
				type);
	}

	@Test
	public void objectAttributeSetInConstructor() throws Throwable {
		String testName = "object_attribute_set_in_constructor";
		Set<Type> expectedType = typeJudgement(listType);

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly. This "
				+ "probably means forward flow analysis didn't follow"
				+ "the value of an instance of A into the constructor's "
				+ "self parameter.", expectedType, type);
	}

	@Test
	public void objectAttributeClassDictFallbackInitDel() throws Throwable {
		String testName = "object_attribute_class_dict_fallback_init_del";

		ScopedPrintNode node = findPrintNode(testName,
				"i_am_really_the_instance_var");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "i_am_really_the_class_var");
		type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributeClassDictFallbackWrite() throws Throwable {
		String testName = "object_attribute_class_dict_fallback_write";

		ScopedPrintNode node = findPrintNode(testName,
				"i_am_really_the_instance_var");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);

		node = findPrintNode(testName, "i_am_really_the_class_var");
		type = engine.typeOf(node.site());

		assertEquals("Attribute's type not inferred correctly", expectedType,
				type);
	}

	@Test
	public void objectAttributeIndependent() throws Throwable {
		TestModule test = newTestModule("object_attribute_independent");

		Set<Type> expectedType = Collections.emptySet();

		Result<Type> type = engine.typeOf(test.printNode("what_am_i").site());

		assertEquals("Attribute's type not BOTTOM. This "
				+ "probably means the assignment to the object instance "
				+ "polluted the inferred type of the member in "
				+ "the class object.", expectedType, type);
	}

	@Test
	public void objectAttributeIndependentSelf() throws Throwable {
		TestModule test = newTestModule("object_attribute_independent_self");

		Set<Type> expectedType = Collections.emptySet();

		Result<Type> type = engine.typeOf(test.printNode("what_am_i").site());

		assertEquals("Attribute's type not BOTTOM. This "
				+ "probably means the assignment to the object instance "
				+ "polluted the inferred type of the member in "
				+ "the class object.", expectedType, type);
	}

	@Test
	public void objectAttributeSharingOneWay() throws Throwable {
		TestModule test = newTestModule("object_attribute_sharing_one_way");

		Set<Type> expectedType = Collections.singleton(stringType);

		Result<Type> type = engine.typeOf(test.printNode("what_am_i_class")
				.site());

		assertEquals("Attribute's type not inferred correctly. This "
				+ "probably means the assignment to the object instance "
				+ "polluted the inferred type of the member in "
				+ "the class object.", expectedType, type);

		type = engine.typeOf(test.printNode("what_am_i_instance").site());

		expectedType = new HashSet<Type>();
		expectedType.add(stringType);
		expectedType.add(listType);

		assertEquals("Attribute's type not inferred correctly.", expectedType,
				type);
	}

	@Test
	public void objectAttributeSharingOneWayFunction() throws Throwable {
		TestModule test = newTestModule("object_attribute_sharing_one_way_function");

		ClassCO classA = test.moduleLevelClass("A");
		Type methodF = new TBoundMethod(nestedFunction(classA, "f"),
				new TObject(classA));
		Type functionG = new TFunction(test.moduleLevelFunction("g"));

		Result<Type> type = engine.typeOf(test.printNode("what_am_i_class")
				.site());

		Set<Type> expectedType = Collections.singleton(methodF);

		assertEquals("Class's method type not inferred correctly. It "
				+ "probably got confused by the method being assigned "
				+ "to an instance of the class", expectedType, type);

		type = engine.typeOf(test.printNode("what_am_i_instance").site());

		expectedType = new HashSet<Type>();
		expectedType.add(methodF);
		expectedType.add(functionG);

		assertEquals("Instance method's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void importModule() throws Throwable {
		String testName = "import_module";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = typeJudgement(new TModule(
				model.lookup(ImportPath.fromDottedName("import_module_aux"))));

		assertEquals("Imported module's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void importModuleAs() throws Throwable {
		String testName = "import_module_as";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = typeJudgement(new TModule(
				model.lookup(ImportPath.fromDottedName("import_module_aux"))));

		assertEquals("Imported module's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void importFunction() throws Throwable {
		String testName = "import_function";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = typeJudgement(new TFunction(nestedFunction(
				model.lookup(ImportPath.fromDottedName("import_function_aux")),
				"fun")));

		assertEquals("Imported function's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void importFunctionAs() throws Throwable {
		String testName = "import_function_as";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = typeJudgement(new TFunction(nestedFunction(
				model.lookup(ImportPath.fromDottedName("import_function_aux")),
				"fun")));

		assertEquals("Imported function's type not inferred correctly",
				expectedType, type);
	}

	@Test
	public void none() throws Throwable {
		String testName = "none";
		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		Set<Type> expectedType = typeJudgement(noneType);

		assertEquals("Function parameter's type not inferred "
				+ "correctly. This probably means that the analysis didn't "
				+ "realise the function is imported into another "
				+ "module and called from there with a different type "
				+ "of parameter.", expectedType, type);
	}

	@Test
	public void inheritedMethod() throws Throwable {
		String testName = "inherited_method";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_parent");
		Result<Type> type = engine.typeOf(node.site());

		ClassCO superclass = moduleLevelClass(node, "B");
		FunctionCO unboundMethod = nestedFunction(superclass, "m");

		ClassCO klass = moduleLevelClass(node, "C");
		Set<Type> expectedType = typeJudgement(new TBoundMethod(unboundMethod,
				new TObject(klass)));

		assertEquals("Didn't infer inherited method type correctly. "
				+ "Probably forgot to look in the superclass.", expectedType,
				type);

		node = findPrintNode(testName, "what_am_i_grandparent");
		type = engine.typeOf(node.site());

		klass = moduleLevelClass(node, "D");
		expectedType = typeJudgement(new TBoundMethod(unboundMethod,
				new TObject(klass)));

		assertEquals("Didn't infer inherited method type correctly. "
				+ "Probably forgot to look in the grandparent superclass.",
				expectedType, type);
	}

	@Test
	public void inheritedMethodOverride() throws Throwable {
		String testName = "inherited_method_override";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_overridden");
		Result<Type> type = engine.typeOf(node.site());

		ClassCO klassA = moduleLevelClass(node, "A");
		FunctionCO unboundMethodA = nestedFunction(klassA, "m");

		Set<Type> am = typeJudgement(new TBoundMethod(unboundMethodA,
				new TObject(klassA)));

		ClassCO klassB = moduleLevelClass(node, "B");
		FunctionCO unboundMethodB = nestedFunction(klassB, "m");
		Set<Type> bm = typeJudgement(new TBoundMethod(unboundMethodB,
				new TObject(klassB)));

		assertEquals("Didn't infer inherited method type correctly. "
				+ "Overriding the method shouldn't change the "
				+ "inferred type of an instance of the superclass.", am, type);

		node = findPrintNode(testName, "what_am_i");
		type = engine.typeOf(node.site());

		assertEquals("Didn't infer inherited method type correctly. "
				+ "Probably saw the inherited method and forgot "
				+ "that it is excluded because it is overridden.", bm, type);

		node = findPrintNode(testName, "what_am_i_parent");
		ClassCO klassC = moduleLevelClass(node, "C");
		Set<Type> cm = typeJudgement(new TBoundMethod(unboundMethodB,
				new TObject(klassC)));
		type = engine.typeOf(node.site());

		assertEquals("Didn't infer inherited method type correctly. "
				+ "Probably forgot to look in the superclass.", cm, type);

		node = findPrintNode(testName, "what_am_i_grandparent");
		ClassCO klassD = moduleLevelClass(node, "D");
		Set<Type> dm = typeJudgement(new TBoundMethod(unboundMethodB,
				new TObject(klassD)));
		type = engine.typeOf(node.site());

		assertEquals("Didn't infer inherited method type correctly. "
				+ "Probably forgot to look in the grandparent superclass.", dm,
				type);
	}

	@Test
	public void listTypeEscape() throws Throwable {
		String testName = "list_type_escape";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals(
				"We shouldn't be able to infer a type for items in a list.",
				TopT.INSTANCE, type);
	}

	@Test
	public void dictTypeEscape() throws Throwable {
		String testName = "dict_type_escape";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("We shouldn't be able to infer a type for items in "
				+ "a dictionary.", TopT.INSTANCE, type);
	}

	@Test
	public void forLoopTarget() throws Throwable {
		String testName = "for_loop_target";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("We shouldn't be able to infer a type for items pulled "
				+ "out of an iterable by a for loop.", TopT.INSTANCE, type);
	}

	@Test
	public void forLoopTupleTarget() throws Throwable {
		String testName = "for_loop_tuple_target";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i_x");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("We shouldn't be able to infer a type for items pulled "
				+ "out of an iterable by a for loop.", TopT.INSTANCE, type);

		node = findPrintNode(testName, "what_am_i_y");
		type = engine.typeOf(node.site());

		assertEquals("We shouldn't be able to infer a type for items pulled "
				+ "out of an iterable by a for loop.", TopT.INSTANCE, type);
	}

	@Test
	public void listComprehensionTarget() throws Throwable {
		String testName = "list_comprehension_target";

		ScopedAstNode node = findNode(testName, "what_am_i");
		ListComp listComp = (ListComp) node.getNode();
		Result<Type> type = engine.typeOf(new ModelSite<exprType>(listComp.elt,
				node.getScope()));

		assertEquals("We shouldn't be able to infer a type for items pulled "
				+ "out of an iterable by a for loop.", TopT.INSTANCE, type);
	}

	@Test
	public void flowThroughMember() throws Throwable {
		String testName = "flow_through_member";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Object probably not tracked through other "
				+ "object's member.", Collections.singleton(integerType), type);
	}

	@Test
	public void flowThroughMemberApart() throws Throwable {
		String testName = "flow_through_member_apart";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());

		assertEquals("Object probably not tracked through other "
				+ "object's member because it is defined in one code "
				+ "object and accessed in another.",
				Collections.singleton(integerType), type);
	}

	@Test
	public void virtualMethodCall() throws Throwable {
		String testName = "virtual_method_call";

		ScopedPrintNode node = findPrintNode(testName, "what_am_i");
		Result<Type> type = engine.typeOf(node.site());
		Set<Type> expectedType = Collections.<Type> singleton(new TObject(
				moduleLevelClass(node, "S")));

		assertEquals("'self' probably not tracked properly as it arrives "
				+ "through a 'virtual' call.", expectedType, type);
	}

	private ScopedAstNode findNode(String moduleName, String tag)
			throws Exception {
		return new TaggedNodeAndScopeFinder(model.loadModule(moduleName)
				.codeObject(), tag).getTaggedNode();
	}

	private ScopedPrintNode findPrintNode(String moduleName, String tag)
			throws Exception {
		return ScopedPrintNode.findPrintNode(model, moduleName, tag);
	}

	private Set<Type> typeJudgement(Type... types) {
		return new HashSet<Type>(Arrays.asList(types));
	}

	private ClassCO builtinClass(String name) {
		return nestedClass(model.getTopLevel().codeObject(), name);
	}

	private ClassCO moduleLevelClass(ScopedPrintNode node, String name) {
		return nestedClass(node.enclosingModule(), name);
	}

	private FunctionCO moduleLevelFunction(ScopedPrintNode node, String name) {
		return nestedFunction(node.enclosingModule(), name);
	}

	private FunctionCO nestedFunction(CodeObject parent, String name) {
		CodeObject codeObject = nestedObjectHelper(parent, name, "function");
		assertTrue("Found a top-level declaration called '" + name
				+ "' but it's the wrong type.",
				codeObject instanceof FunctionCO);

		return (FunctionCO) codeObject;
	}

	private ClassCO nestedClass(CodeObject parent, String name) {
		CodeObject codeObject = nestedObjectHelper(parent, name, "class");
		assertTrue("Found a top-level declaration called '" + name
				+ "' but it's the wrong type.", codeObject instanceof ClassCO);

		return (ClassCO) codeObject;
	}

	private NestedCodeObject nestedObjectHelper(CodeObject parent, String name,
			String lookupType) {

		NestedCodeObjects codeObjects = parent.nestedCodeObjects()
				.namedCodeObjectsDeclaredAs(name);
		assertFalse("No object declared as '" + name + "'.",
				codeObjects.isEmpty());
		assertEquals("Test error: assuming a single expected " + lookupType
				+ " declared as '" + name + "' but there are several.", 1,
				codeObjects.size());

		return codeObjects.iterator().next();
	}

	private TestModule newTestModule(String testName) throws Throwable {
		return new TestModule(testName, model);
	}
}
