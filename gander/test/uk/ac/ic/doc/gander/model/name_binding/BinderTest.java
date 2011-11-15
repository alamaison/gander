package uk.ac.ic.doc.gander.model.name_binding;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.ScopedPrintNode;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.name_binding.Binder;

public final class BinderTest {
	private static final String TEST_FOLDER = "../python_test_code/name_binding";

	private MutableModel model;

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
	}

	/**
	 * A variable that refers to the local binding despite the same name being
	 * in the global namespace.
	 */
	@Test
	public void globalHiddenByLocal() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_hidden_by_local",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it picked up the global i instead "
				+ "of the local one bound in the local scope by assignment",
				node.getGlobalNamespace().lookupMember("f"), scope);
	}

	/**
	 * A variable that refers to its parent's binding despite the same name
	 * being defined in the global namespace.
	 */
	@Test
	public void globalHiddenByParent() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_hidden_by_parent",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it picked up the global i instead "
				+ "of the one bound in the parent's scope by assignment", node
				.getGlobalNamespace().lookupMember("f"), scope);
	}

	/**
	 * A variable that refers to its grandparent's binding despite the same name
	 * being defined in the global namespace.
	 */
	@Test
	public void globalHiddenByGrandparent() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_hidden_by_grandparent",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it picked up the global i instead "
				+ "of the one bound in the parent's scope by assignment", node
				.getGlobalNamespace().lookupMember("f"), scope);
	}

	/**
	 * A global that is not defined in the global codeblock should still be
	 * bound using the global namespace.
	 */
	@Test
	public void globalDeclAsOnlyDefinition() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_decl_as_only_definition",
				"whose_am_i_locally");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it saw there was no definition "
				+ "in the global code block and assumed it was a builtin.",
				node.getGlobalNamespace(), scope);

		node = findPrintNode("global_decl_as_only_definition",
				"whose_am_i_globally");
		scope = resolveBindingScope(node);

		assertEquals("This probably means it saw there was no definition "
				+ "in the global code block and assumed it was a builtin.",
				node.getGlobalNamespace(), scope);
	}

	@Test
	public void globalDeclDistractedByLocalDefinition() throws Throwable {
		ScopedPrintNode node = findPrintNode(
				"global_decl_distracted_by_local_definition",
				"whose_am_i_locally");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it saw there was no definition "
				+ "in the global code block but looked for other bindings "
				+ "of the name in the module and failed to filter "
				+ "ones that weren't global.", model.getTopLevel(), scope);

		node = findPrintNode("global_decl_distracted_by_local_definition",
				"whose_am_i_globally");
		scope = resolveBindingScope(node);

		assertEquals("This probably means it saw there was no definition "
				+ "in the global code block but looked for other bindings "
				+ "of the name in the module and failed to filter "
				+ "ones that weren't global.", model.getTopLevel(), scope);
	}

	/**
	 * Symbols that aren't found in the enclosing module (so presumably are in
	 * the builtin namespace are still staticlly bound to the enclosing module
	 * (global) namespace because the global vs builtin decision is made at
	 * runtime not statically.
	 */
	@Test
	public void builtinNamespace() throws Throwable {
		ScopedPrintNode node = findPrintNode("builtin_namespace", "whose_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals(node.getScope(), scope);
	}

	/**
	 * A variable that refers to its local binding despite the same name being
	 * defined in its enclosing scope as well as the global namespace.
	 */
	@Test
	public void parentHiddenByLocal() throws Throwable {
		ScopedPrintNode node = findPrintNode("parent_hidden_by_local",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it picked up the global i instead "
				+ "of the local one bound in the local scope by assignment",
				node.getScope(), scope);
	}

	/**
	 * A variable that is bound to the global name despite the same name being
	 * defined in the enclosing scope's namespace because of the presence of the
	 * global keyword in the local scope
	 */
	@Test
	public void globalDeclarationLocal() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_decl_local", "what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it ignored the 'global' statement "
				+ "and picked up the one bound in the local scope "
				+ "by assignment", node.getGlobalNamespace(), scope);
	}

	/**
	 * A variable that is bound to the global name despite the same name being
	 * defined in an enclosing scope's namespace because of the presence of the
	 * global keyword in a parent scope.
	 */
	@Test
	public void globalDeclarationInAncestor() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_decl_in_ancestor",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it ignored the 'global' statement "
				+ "in the parent scope and picked up the one bound in the "
				+ "local scope by assignment", node.getGlobalNamespace(), scope);
	}

	/**
	 * A variable that is bound to the global name despite the same name being
	 * defined in an enclosing scope's namespace because of the presence of the
	 * global keyword in a grandparent scope.
	 */
	@Test
	public void globalDeclarationInGrandcestor() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_decl_in_grandcestor",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably "
				+ "means it ignored the 'global' statement in the grandparent "
				+ "scope and picked up the one bound in the local scope "
				+ "by assignment", node.getGlobalNamespace(), scope);
	}

	/**
	 * A variable that is bound to the local name despite the presence of the
	 * global keyword in a nested scope.
	 */
	@Test
	public void globalDeclarationInChild() throws Throwable {
		ScopedPrintNode node = findPrintNode("global_decl_in_child",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably "
				+ "means it saw the 'global' statement in the child "
				+ "scope and mistakenly let it affect the binding "
				+ "in the parent scope", node.getGlobalNamespace()
				.lookupMember("f"), scope);
	}

	/**
	 * A variable that, despite the ancestral global declaration, is bound
	 * locally because it is defined locally. Outside the scope of this
	 * definition the global keyword affects nested blocks as usual.
	 */
	@Test
	public void localOverridingGlobalDeclarationInAncestor() throws Throwable {
		ScopedPrintNode node = findPrintNode(
				"local_overriding_global_decl_in_ancestor", "what_am_i_locally");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably means it saw its parent's 'global' "
				+ "statement but didn't pay attention to the local "
				+ "definition which overrides inherited global binding", node
				.getScope(), scope);

		node = findPrintNode("local_overriding_global_decl_in_ancestor",
				"what_am_i_outside");
		scope = resolveBindingScope(node);

		assertEquals("This probably means the definition in h() was bound "
				+ "to the global instead of the local causing the string "
				+ "assignment to affect the outer i's binding scope somehow",
				node.getGlobalNamespace(), scope);
	}

	/**
	 * Binds a variable in a method to the scope *outside* its defining class
	 * despite its class defining a variable of the same name.
	 */
	@Test
	public void classScopingAnomaly() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_scoping_anomaly",
				"what_am_i_in_a_method");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably "
				+ "means that the variable in the method was bound to "
				+ "the variable defined in the class when it should have "
				+ "skipped that scope and been bound to the first enclosing "
				+ "non-class scope.", node.getGlobalNamespace(), scope);

		node = findPrintNode("class_scoping_anomaly", "what_am_i_in_the_class");
		scope = resolveBindingScope(node);

		assertEquals("Within the class's code block, i should refer "
				+ "to the one defined the the class's namespace", node
				.getGlobalNamespace().lookupMember("A"), scope);
	}

	/**
	 * Binds a variable in a method to the *nearest* non-class enclosing scope
	 * despite both classes defining a variable of the same name.
	 */
	@Test
	public void classScopingAnomalyDeep() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_scoping_anomaly_deep",
				"what_am_i_in_a_method");
		Namespace scope = resolveBindingScope(node);

		assertEquals("This probably "
				+ "means that the variable in the method was bound to "
				+ "the variable defined in the class when it should have "
				+ "skipped that scope and been bound to the first enclosing "
				+ "non-class scope.", node.getGlobalNamespace(), scope);

		node = findPrintNode("class_scoping_anomaly_deep",
				"what_am_i_in_the_parent_class");
		scope = resolveBindingScope(node);

		assertEquals("Within the class's code block, i should refer "
				+ "to the one defined the the class's namespace", node
				.getGlobalNamespace().getClasses().get("A").lookupMember("B"),
				scope);

		node = findPrintNode("class_scoping_anomaly_deep",
				"what_am_i_in_the_grandparent_class");
		scope = resolveBindingScope(node);

		assertEquals("Within the class's code block, i should refer "
				+ "to the one defined the the class's namespace", node
				.getGlobalNamespace().lookupMember("A"), scope);
	}

	/**
	 * Class scopes are special in that the scope of their variables doesn't go
	 * beyond their own code block. Therefore the nested class body doesn't see
	 * the enclosing class's definition.
	 */
	@Test
	public void classScopeNestedClass() throws Throwable {
		ScopedPrintNode node = findPrintNode("class_scope_nested_class",
				"what_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("Variable not bound correctly. This probably "
				+ "means that the variable in the nested class was "
				+ "bound to the variable defined in the enclosing "
				+ "class when it should have skipped that scope and been "
				+ "bound to the first enclosing non-class scope.", node
				.getGlobalNamespace(), scope);
	}

	@Test
	public void subtleGlobalHiding() throws Throwable {
		ScopedPrintNode node = findPrintNode("subtle_hiding_global", "who_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals("Variable not bound in function f's namespace. This "
				+ "probably means it picked up the global i instead of the "
				+ "local one declared in the for loop.", node
				.getGlobalNamespace().lookupMember("f"), scope);
	}

	/**
	 * Function parameters bind using the scope of their containing function.
	 */
	@Test
	public void functionParameters() throws Throwable {
		ScopedPrintNode node = findPrintNode("function_parameters",
				"whose_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals(
				"Function parameter not bound using the function's namespace.",
				node.getScope(), scope);
	}

	/**
	 * Bindings in a nested scope can't affect the value of the outer scope
	 * except globals.
	 */
	@Test
	public void nestedFunction() throws Throwable {
		ScopedPrintNode node = findPrintNode("nested_function",
				"who_am_i_inside");
		Namespace scope = resolveBindingScope(node);

		assertEquals("Nested use of name resolved to wrong scope.", node
				.getScope(), scope);

		node = findPrintNode("nested_function", "who_am_i_outside");
		scope = resolveBindingScope(node);

		assertEquals("Outer use of name resolved to wrong scope.", node
				.getScope(), scope);
	}

	/**
	 * The binding in the nested scope shouldn't block the outer use of a
	 * resolving to the global scope.
	 */
	@Test
	public void nestedDefinitionIntereresWithGlobal() throws Throwable {
		ScopedPrintNode node = findPrintNode(
				"nested_definition_interferes_with_global", "who_am_i_inside");
		Namespace scope = resolveBindingScope(node);

		assertEquals("Nested use of name resolved to wrong scope.", node
				.getScope(), scope);

		node = findPrintNode("nested_definition_interferes_with_global",
				"who_am_i_outside");
		scope = resolveBindingScope(node);

		assertEquals("Outer use of name resolved to wrong scope.", node
				.getGlobalNamespace(), scope);
	}

	@Test
	public void imported() throws Throwable {
		String testName = "imported";
		ScopedPrintNode node = findPrintNode(testName, "whose_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals(
				"Name introduced by import statement binds in enclosing scope.",
				node.getScope(), scope);
	}

	@Test
	public void importedAs() throws Throwable {
		String testName = "imported_as";
		ScopedPrintNode node = findPrintNode(testName, "whose_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals(
				"Name introduced by import statement binds in enclosing scope.",
				node.getScope(), scope);
	}

	@Test
	public void importedFrom() throws Throwable {
		String testName = "imported_from";
		ScopedPrintNode node = findPrintNode(testName, "whose_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals(
				"Name introduced by import statement binds in enclosing scope.",
				node.getScope(), scope);
	}

	@Test
	public void importedFromAs() throws Throwable {
		String testName = "imported_from_as";
		ScopedPrintNode node = findPrintNode(testName, "whose_am_i");
		Namespace scope = resolveBindingScope(node);

		assertEquals(
				"Name introduced by import statement binds in enclosing scope.",
				node.getScope(), scope);
	}

	private Namespace resolveBindingScope(ScopedPrintNode node) {
		return Binder.resolveBindingScope(node.getExpressionName(),
				node.getScope(), model).getNamespace();
	}

	private ScopedPrintNode findPrintNode(String moduleName, String tag)
			throws Exception {
		return ScopedPrintNode.findPrintNode(model, moduleName, tag);
	}

}
