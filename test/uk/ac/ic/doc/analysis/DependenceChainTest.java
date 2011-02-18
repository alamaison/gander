package uk.ac.ic.doc.analysis;

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

public class DependenceChainTest extends AbstractTaggedCallTest {

	private static final String TEST_FOLDER = "python_test_code/matching_dom_length/basic";
	private DependenceChain analyser;

	public DependenceChainTest() {
		super(TEST_FOLDER);
	}

	public void initialise(String caseName) throws Throwable {
		super.initialise(caseName);
		analyser = new DependenceChain(module, graph);
	}

	@Test
	public void testBasic() throws Throwable {
		initialise("basic");
		String[][] chains = { { "y.b(tag)", "a", "b" } };
		checkChains(chains);
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
	public void testIgnoreImportedFunctions() throws Throwable {
		initialise("ignore_imported_functions");
		String[][] chains = { { "y.b(tag2)", "a", "b" }, };
		checkChains(chains);
		checkNoChain("sys.getdefaultencoding(tag1)");
	}

	private void checkChains(String[]... descriptors) throws Exception {
		for (String[] descriptor : descriptors) {
			Set<String> expected = new HashSet<String>();
			for (int i = 1; i < descriptor.length; ++i) {
				expected.add(descriptor[i]);
			}
			checkChain(descriptor[0], expected);
		}
	}

	private void checkChain(String taggedCall, Set<String> expected)
			throws Exception {
		Statement statement = findTaggedStatement(taggedCall);
		assertTrue("Unable to find statement tagged in test: '" + taggedCall
				+ "'", statement != null);
		String variable = variableFromTag(taggedCall);

		Iterable<Call> chain = analyser.dependentCalls(
				statement.getCall(), statement.getBlock());

		// Test the chain only includes calls to the single expected variable
		Set<String> variables = variablesInChain(chain);
		assertEquals(1, variables.size());
		assertEquals(variable, variables.iterator().next());

		// Test that all expected calls are in the chain and no unexpected calls
		// are in the chain.
		// TODO: We consider any call with matching name but ignore arguments
		Collection<String> calledMethods = calledMethodsFromChain(chain,
				variable);
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

	private void checkNoChain(String antiTag) throws Exception {
		Statement statement = findTaggedStatement(antiTag);
		assertTrue(
				"Unable to find statement tagged in test: '" + antiTag + "'",
				statement != null);

		Iterable<Call> chain = analyser.dependentCalls(
				statement.getCall(), statement.getBlock());
		assertTrue(
				"Tagged function '" + antiTag
						+ "' has a dependence chain when we don't expect one: "
						+ chain, null == chain);
	}

	private Set<String> calledMethodsFromChain(Iterable<Call> chain,
			String variable) {
		Set<String> methods = new HashSet<String>();
		for (Call call : chain) {
			Attribute fieldAccess = (Attribute) call.func;
			methods.add(((NameTok) fieldAccess.attr).id);
		}
		return methods;
	}

	private Set<String> variablesInChain(Iterable<Call> chain) {
		Set<String> vars = new HashSet<String>();
		for (Call call : chain) {
			Attribute fieldAccess = (Attribute) call.func;
			vars.add(((Name) fieldAccess.value).id);
		}
		return vars;
	}
}
