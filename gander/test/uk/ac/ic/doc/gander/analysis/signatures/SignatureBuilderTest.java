package uk.ac.ic.doc.gander.analysis.signatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.AbstractTaggedCallTest;
import uk.ac.ic.doc.gander.Statement;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;

public class SignatureBuilderTest extends AbstractTaggedCallTest {

	private static final String TEST_FOLDER = "../python_test_code/matching_dom_length/basic";
	private final SignatureBuilder analyser = new SignatureBuilder();

	public SignatureBuilderTest() {
		super(TEST_FOLDER);
	}

	@Override
	public void initialise(String caseName) throws Throwable {
		super.initialise(caseName);
	}

	@Test
	public void testBasic() throws Throwable {
		initialise("basic");
		String[][] chains = { { "y.b(tag)", "a", "b", "c" } };
		checkChains(chains);
	}

	@Test
	public void testBasicRf() throws Throwable {
		initialise("basic");
		String[][] chains = { { "y.b(tag)", "b", "c" } };
		checkChainsRf(chains);
	}

	@Test
	public void testBasicFstr() throws Throwable {
		initialise("basic");
		String[][] chains = { { "y.b(tag)", "a" } };
		checkChainsFstr(chains);
	}

	/**
	 * Assignment to a variable should prevent us including any uses that occur
	 * before it.
	 */
	@Test
	public void testIgnoreUsesBeforeKill() throws Throwable {
		initialise("kills_in_same_block");
		String[][] chains = { { "y.b(tag3)", "b", "c" },
				{ "y.c(tag4)", "b", "c" } };
		checkChains(chains);
	}

	@Test
	public void testIgnoreUsesBeforeKillFstr() throws Throwable {
		initialise("kills_in_same_block");
		String[][] chains = { { "y.b(tag3)" }, { "y.c(tag4)", "b" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testIgnoreUsesBeforeKillRf() throws Throwable {
		initialise("kills_in_same_block");
		String[][] chains = { { "y.b(tag3)", "b", "c" }, { "y.c(tag4)", "c" } };
		checkChainsRf(chains);
	}

	/**
	 * Assignment to a variable should prevent including any uses after the
	 * assignment.
	 */
	@Test
	public void testOnlyIncludeBetweenKills() throws Throwable {
		initialise("kills_in_same_block");
		String[][] chains = { { "y.a(tag1)", "a", "b" },
				{ "y.b(tag2)", "a", "b" } };
		checkChains(chains);
	}

	@Test
	public void testOnlyIncludeBetweenKillsFstr() throws Throwable {
		initialise("kills_in_same_block");
		String[][] chains = { { "y.a(tag1)" }, { "y.b(tag2)", "a" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testOnlyIncludeBetweenKillsRf() throws Throwable {
		initialise("kills_in_same_block");
		String[][] chains = { { "y.a(tag1)", "a", "b" }, { "y.b(tag2)", "b" } };
		checkChainsRf(chains);
	}

	/**
	 * Assignment to one variable shouldn't end our search for uses of another.
	 */
	@Test
	public void testMixedKillsInSameBlock() throws Throwable {
		initialise("mixed_kills_in_same_block");
		String[][] chains = { { "x.a(tag1)", "a" }, { "x.b(tag3)", "b" },
				{ "y.a(tag2)", "a", "m" }, { "y.m(tag4)", "a", "m" }, };
		checkChains(chains);
	}

	@Test
	public void testMixedKillsInSameBlockFstr() throws Throwable {
		initialise("mixed_kills_in_same_block");
		String[][] chains = { { "x.a(tag1)" }, { "x.b(tag3)" },
				{ "y.a(tag2)" }, { "y.m(tag4)", "a" }, };
		checkChainsFstr(chains);
	}

	@Test
	public void testMixedKillsInSameBlockRf() throws Throwable {
		initialise("mixed_kills_in_same_block");
		String[][] chains = { { "x.a(tag1)", "a" }, { "x.b(tag3)", "b" },
				{ "y.a(tag2)", "a", "m" }, { "y.m(tag4)", "m" }, };
		checkChainsRf(chains);
	}

	/**
	 * Variable uses on the RHS of an assignment should still be included.
	 */
	@Test
	public void testAssignValueIsUse() throws Throwable {
		initialise("assign_value_is_use");
		String[][] chains = { { "x.a(tag1)", "a", "b" },
				{ "x.b(tag2)", "a", "b" }, { "x.a(tag3)", "a" }, };
		checkChains(chains);
	}

	/**
	 * Multiple variables on LHS are all killed so prevent searching for uses
	 * past their line.
	 */
	@Test
	public void testMultikill() throws Throwable {
		initialise("multikill");
		String[][] chains = { { "y.a(tag1)", "a" }, { "x.b(tag2)", "b" },
				{ "x.c(tag3)", "c" }, { "y.d(tag4)", "d" }, };
		checkChains(chains);
	}

	/**
	 * Earlier statements that don't dominate the one we're interested in,
	 * shouldn't be considered in dependence chain.
	 */
	@Test
	public void testIncludeOnlyDominators() throws Throwable {
		initialise("include_only_dominators");
		String[][] chains = { { "y.c(tag)", "a", "c" }, };
		checkChains(chains);
	}

	@Test
	public void testIncludeOnlyDominatorsFstr() throws Throwable {
		initialise("include_only_dominators");
		String[][] chains = { { "y.c(tag)", "a" }, };
		checkChainsFstr(chains);
	}

	@Test
	public void testIncludeOnlyDominatorsRf() throws Throwable {
		initialise("include_only_dominators");
		String[][] chains = { { "y.c(tag)", "c" }, };
		checkChainsRf(chains);
	}

	/**
	 * Later statements that don't postdominate the one we're interested in,
	 * shouldn't be considered in dependence chain.
	 */
	@Test
	public void testIncludeOnlyPostdominators() throws Throwable {
		initialise("include_only_postdominators");
		String[][] chains = { { "y.a(tag)", "a", "c" }, };
		checkChains(chains);
	}

	@Test
	public void testIncludeOnlyPostdominatorsFstr() throws Throwable {
		initialise("include_only_postdominators");
		String[][] chains = { { "y.a(tag)" }, };
		checkChainsFstr(chains);
	}

	@Test
	public void testIncludeOnlyPostdominatorsRf() throws Throwable {
		initialise("include_only_postdominators");
		String[][] chains = { { "y.a(tag)", "a", "c" }, };
		checkChainsRf(chains);
	}

	/**
	 * The chain should only inlude earlier statement that dominate and later
	 * statements postdominate the one we're interested in.
	 */
	@Test
	public void testIncludeDomAndPostdom() throws Throwable {
		initialise("include_dom_and_postdom");
		String[][] chains = { { "y.b(tag)", "a", "b", "c" }, };
		checkChains(chains);
	}

	@Test
	public void testIncludeDomAndPostdomFstr() throws Throwable {
		initialise("include_dom_and_postdom");
		String[][] chains = { { "y.b(tag)", "a" }, };
		checkChainsFstr(chains);
	}

	@Test
	public void testIncludeDomAndPostdomRf() throws Throwable {
		initialise("include_dom_and_postdom");
		String[][] chains = { { "y.b(tag)", "b", "c" }, };
		checkChainsRf(chains);
	}

	/**
	 * Even though an assignment may happen in a non-dominating but reachable
	 * block, it should still prevent the search for variable uses crossing it.
	 */
	@Test
	public void testAssignmentInNonDomBlock() throws Throwable {
		initialise("assignment_in_non_dom_block");
		String[][] chains = { { "y.a(tag1)", "a", "b" },
				{ "y.b(tag2)", "a", "b" }, { "y.c(tag3)", "c", "d" },
				{ "y.d(tag4)", "c", "d" }, };
		checkChains(chains);
	}

	@Test
	public void testFunctionDefIsVariableDecl() throws Throwable {
		initialise("function_def_is_variable_decl", 1);
		String[][] chains = { { "z.b(tag1)", "a", "b", "c" },
				{ "nested.__call__(tag2)", "__call__" } };
		checkChains(chains);
		checkNoChain("y.o(tag_nested)");
	}

	@Test
	public void testLambdaIsObjectIgnoreBody() throws Throwable {
		initialise("lambda_is_object_ignore_body", 1);
		String[][] chains = { { "z.b(tag1)", "a", "b", "c" },
				{ "l.__call__(tag2)", "__call__", "p" } };
		checkChains(chains);
		checkNoChain("y.a(tag_in_lambda1)", "y.b(tag_in_lambda2)");
	}

	@Test
	public void testNestedCalls() throws Throwable {
		initialise("nested_calls", 1);
		String[][] chains = { { "g.f(inner_tag)", "e", "f" },
				{ "g.e(outer_tag)", "e", "f" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallAfter() throws Throwable {
		initialise("dig_into_call_after", 1);
		String[][] chains = { { "y.b(tag)", "a", "b", "p", "q" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallAfterFstr() throws Throwable {
		initialise("dig_into_call_after", 1);
		String[][] chains = { { "y.b(tag)", "a" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testDigIntoCallAfterRf() throws Throwable {
		initialise("dig_into_call_after", 1);
		String[][] chains = { { "y.b(tag)", "b", "p", "q" } };
		checkChainsRf(chains);
	}

	@Test
	public void testDigIntoCallBefore() throws Throwable {
		initialise("dig_into_call_before", 1);
		String[][] chains = { { "y.b(tag)", "a", "b", "p", "q" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallBeforeFstr() throws Throwable {
		initialise("dig_into_call_before", 1);
		String[][] chains = { { "y.b(tag)", "a", "p", "q" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testDigIntoCallBeforeRf() throws Throwable {
		initialise("dig_into_call_before", 1);
		String[][] chains = { { "y.b(tag)", "b" } };
		checkChainsRf(chains);
	}

	@Test
	public void testDigIntoCallOtherBlockAfter() throws Throwable {
		initialise("dig_into_call_other_block_after", 3);
		String[][] chains = { { "y.b(tag)", "a", "b", "p", "q" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallOtherBlockAfterFstr() throws Throwable {
		initialise("dig_into_call_other_block_after", 3);
		String[][] chains = { { "y.b(tag)", "a" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testDigIntoCallOtherBlockAfterRf() throws Throwable {
		initialise("dig_into_call_other_block_after", 3);
		String[][] chains = { { "y.b(tag)", "b", "p", "q" } };
		checkChainsRf(chains);
	}

	@Test
	public void testDigIntoCallOtherBlockBefore() throws Throwable {
		initialise("dig_into_call_other_block_before", 3);
		String[][] chains = { { "y.b(tag)", "a", "b", "p", "q" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallOtherBlockBeforeFstr() throws Throwable {
		initialise("dig_into_call_other_block_before", 3);
		String[][] chains = { { "y.b(tag)", "a", "p", "q" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testDigIntoCallOtherBlockBeforeRf() throws Throwable {
		initialise("dig_into_call_other_block_before", 3);
		String[][] chains = { { "y.b(tag)", "b" } };
		checkChainsRf(chains);
	}

	@Test
	public void testDigIntoCallAfterWithSomeNonPostdomStatements()
			throws Throwable {
		initialise("dig_into_call_after_with_some_non_postdom_statements", 1);
		String[][] chains = { { "g.b(tag)", "a", "b", "r", "t" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallAfterWithSomeNonPostdomStatementsFstr()
			throws Throwable {
		initialise("dig_into_call_after_with_some_non_postdom_statements", 1);
		String[][] chains = { { "g.b(tag)", "a" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testDigIntoCallAfterWithSomeNonPostdomStatementsRf()
			throws Throwable {
		initialise("dig_into_call_after_with_some_non_postdom_statements", 1);
		String[][] chains = { { "g.b(tag)", "b", "r", "t" } };
		checkChainsRf(chains);
	}

	@Test
	public void testDigIntoCallBeforeWithSomeNonPostdomStatements()
			throws Throwable {
		initialise("dig_into_call_before_with_some_non_postdom_statements", 1);
		String[][] chains = { { "g.b(tag)", "a", "b", "r", "t" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallBeforeWithSomeNonPostdomStatementsFstr()
			throws Throwable {
		initialise("dig_into_call_before_with_some_non_postdom_statements", 1);
		String[][] chains = { { "g.b(tag)", "a", "r", "t" } };
		checkChainsFstr(chains);
	}

	@Test
	public void testDigIntoCallBeforeWithSomeNonPostdomStatementsRf()
			throws Throwable {
		initialise("dig_into_call_before_with_some_non_postdom_statements", 1);
		String[][] chains = { { "g.b(tag)", "b" } };
		checkChainsRf(chains);
	}

	@Test
	public void testDigTwoLevels() throws Throwable {
		initialise("dig_two_levels", 1);
		String[][] chains = { { "y.a(tag)", "a", "bob", "sally", "r", "t" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallImportedFromSiblingModule() throws Throwable {
		initialise("dig_into_call_imported_from_sibling_module", 1);
		String[][] chains = { { "y.a(tag)", "a", "f", "g", "billy" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallImportedFromSiblingModuleDeep() throws Throwable {
		initialise("dig_into_call_imported_from_sibling_module_deep", 1);
		String[][] chains = { { "y.a(tag)", "a", "f", "g", "goose" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallImportedFromPackageModule() throws Throwable {
		initialise("dig_into_call_imported_from_package_module", 1);
		String[][] chains = { { "y.a(tag)", "a", "show", "baaa" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoCallImportedFromPackageModuleClever()
			throws Throwable {
		initialise("dig_into_call_imported_from_package_module_clever", 1);
		String[][] chains = { { "y.a(tag)", "a", "gurgle", "f", "g", "goose" } };
		checkChains(chains);
	}

	@Test
	public void testCallBuiltin() throws Throwable {
		initialise("call_builtin", 1);
		String[][] chains = { { "y.a(tag)", "a", "__len__" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoRecursiveFunction() throws Throwable {
		initialise("dig_into_recursive_function", 1);
		String[][] chains = { { "y.a(tag)", "a", "z" } };
		checkChains(chains);
	}

	@Test
	public void testDigIntoIndirectlyRecursiveFunction() throws Throwable {
		initialise("dig_into_indirectly_recursive_function", 1);
		String[][] chains = { { "y.a(tag)", "a", "p", "q" } };
		checkChains(chains);
	}

	private void checkChains(String[]... descriptors) throws Exception {
		for (String[] descriptor : descriptors) {
			Set<String> expected = new HashSet<String>();
			for (int i = 1; i < descriptor.length; ++i) {
				expected.add(descriptor[i]);
			}
			checkChain(descriptor[0], expected, true, true);
		}
	}

	private void checkChainsFstr(String[]... descriptors) throws Exception {
		for (String[] descriptor : descriptors) {
			Set<String> expected = new HashSet<String>();
			for (int i = 1; i < descriptor.length; ++i) {
				expected.add(descriptor[i]);
			}
			checkChain(descriptor[0], expected, false, true);
		}
	}

	private void checkChain(String taggedCall, Set<String> expected,
			boolean includeRequiredFeatures, boolean includeFstr)
			throws Exception {
		Statement statement = findTaggedStatement(taggedCall);
		assertTrue("Unable to find statement tagged in test: '" + taggedCall
				+ "'", statement != null);

		Iterable<Call> chain = analyser.signature(
				extractMethodCallTarget(statement.getCall()), statement
						.getBlock(), function, new TypeResolver(
						new ZeroCfaTypeEngine()), includeRequiredFeatures,
				includeFstr);

		// Test that all expected calls are in the chain and no unexpected calls
		// are in the chain.
		// TODO: We consider any call with matching name but ignore arguments
		Collection<String> calledMethods = calledMethodsFromChain(chain);
		assertEquals(
				"Expected dependence chain doesn't match method found in the "
						+ "chain produced by the analyser", expected,
				calledMethods);
	}

	private void checkNoChain(String... antiTags) throws Exception {
		for (String antiTag : antiTags) {
			checkNoChain(antiTag);
		}
	}

	private void checkChainsRf(String[]... descriptors) throws Exception {
		for (String[] descriptor : descriptors) {
			Set<String> expected = new HashSet<String>();
			for (int i = 1; i < descriptor.length; ++i) {
				expected.add(descriptor[i]);
			}
			checkChain(descriptor[0], expected, true, false);
		}
	}

	private void checkNoChain(String antiTag) throws Exception {
		Statement statement = findTaggedStatement(antiTag);
		assertTrue(
				"Unable to find statement tagged in test: '" + antiTag + "'",
				statement != null);

		Set<Call> chain = analyser.signature(extractMethodCallTarget(statement
				.getCall()), statement.getBlock(), function, new TypeResolver(
				new ZeroCfaTypeEngine()), true, true);
		assertTrue(
				"Tagged function '" + antiTag
						+ "' has a dependence chain when we don't expect one: "
						+ chain, chain.isEmpty());
	}

	private static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	private static NameTok extractMethodCallName(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (NameTok) fieldAccess.attr;
	}

	private Set<String> calledMethodsFromChain(Iterable<Call> chain) {
		Set<String> methods = new HashSet<String>();
		for (Call call : chain) {
			methods.add(extractMethodCallName(call).id);
		}
		return methods;
	}
}
