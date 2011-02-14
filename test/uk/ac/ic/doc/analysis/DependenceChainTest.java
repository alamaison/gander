package uk.ac.ic.doc.analysis;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;

public class DependenceChainTest extends AbstractTaggedCallTest {

	private static final String TEST_FOLDER = "python_test_code/matching_dom_length/basic";
	private DependenceChain analyser;

	public DependenceChainTest() {
		super(TEST_FOLDER);
	}

	public void initialise(String caseName) throws Throwable {
		super.initialise(caseName);
		analyser = new DependenceChain(graph);
	}

	@Test
	public void testBasic() throws Throwable {
		initialise("basic");
		checkChain("y.b(tag)", result("a", "b"));
	}

	/**
	 * Assignment to a variable should prevent us including any uses that occur
	 * before it.
	 */
	@Test
	public void testIgnoreUsesBeforeKill() throws Throwable {
		initialise("kills_in_same_block");
		checkChain("y.b(tag3)", result("b", "c"));
		checkChain("y.c(tag4)", result("b", "c"));
	}

	/**
	 * Assignment to a variable should prevent including any uses after the
	 * assignment.
	 */
	@Test
	public void testOnlyIncludeBetweenKills() throws Throwable {
		initialise("kills_in_same_block");
		checkChain("y.a(tag1)", result("a", "b"));
		checkChain("y.b(tag2)", result("a", "b"));
	}

	/**
	 * Assignment to one variable shouldn't end our search for uses of another.
	 */
	@Test
	public void testMixedKillsInSameBlock() throws Throwable {
		initialise("mixed_kills_in_same_block");
		checkChain("x.a(tag1)", result("a"));
		checkChain("x.b(tag3)", result("b"));
		checkChain("y.a(tag2)", result("a", "m"));
		checkChain("y.m(tag4)", result("a", "m"));
	}

	/**
	 * Variable uses on the RHS of an assignment should still be included.
	 */
	@Test
	public void testAssignValueIsUse() throws Throwable {
		initialise("assign_value_is_use");
		checkChain("x.a(tag1)", result("a", "b"));
		checkChain("x.b(tag2)", result("a", "b"));
		checkChain("x.a(tag3)", result("a"));
	}

	/**
	 * Multiple variables on LHS are all killed so prevent searching for uses
	 * past their line.
	 */
	@Test
	public void testMultikill() throws Throwable {
		initialise("multikill");
		checkChain("y.a(tag1)", result("a"));
		checkChain("x.b(tag2)", result("b"));
		checkChain("x.c(tag3)", result("c"));
		checkChain("y.d(tag4)", result("d"));
	}

	/**
	 * Earlier statements that don't dominate the one we're interested in,
	 * shouldn't be considered in dependence chain.
	 */
	@Test
	public void testIncludeOnlyDominators() throws Throwable {
		initialise("include_only_dominators");
		checkChain("y.c(tag)", result("a", "c"));
	}

	/**
	 * Later statements that don't postdominate the one we're interested in,
	 * shouldn't be considered in dependence chain.
	 */
	@Test
	public void testIncludeOnlyPostdominators() throws Throwable {
		initialise("include_only_postdominators");
		checkChain("y.a(tag)", result("a", "c"));
	}

	/**
	 * The chain should only inlude earlier statement that dominate and later
	 * statements postdominate the one we're interested in.
	 */
	@Test
	public void testIncludeDomAndPostdom() throws Throwable {
		initialise("include_dom_and_postdom");
		checkChain("y.b(tag)", result("a", "b", "c"));
	}

	/**
	 * Even though an assignment may happen in a non-dominating but reachable
	 * block, it should still prevent the search for variable uses crossing it.
	 */
	@Test
	public void testAssignmentInNonDomBlock() throws Throwable {
		initialise("assignment_in_non_dom_block");
		checkChain("y.a(tag1)", result("a", "b"));
		checkChain("y.b(tag2)", result("a", "b"));
		checkChain("y.c(tag3)", result("c", "d"));
		checkChain("y.d(tag4)", result("c", "d"));
	}

	private void checkChain(String taggedCall, Set<String> expected)
			throws Exception {
		Statement statement = findTaggedStatement(taggedCall);
		assertTrue("Unable to find statement tagged in test: '" + taggedCall
				+ "'", statement != null);
		String variable = variableFromTag(taggedCall);

		Iterable<Call> chain = analyser.dependentStatements(
				statement.getCall(), statement.getBlock());
		// TODO: We consider any call with matching name but ignore arguments
		for (String callName : expected) {
			checkCallInChain(variable, callName, chain);
		}

		for (Call call : chain) {
			// Test only expected variable included in chain
			assertTrue(
					"Dependence chain includes a method called on variable '"
							+ TaggedBlockFinder.extractMethodCallTarget(call)
							+ "' but should only include calls that target '"
							+ variable + "'",
					TaggedBlockFinder.isMethodCallTarget(variable, call));

			// Test only expected calls included in chain
			boolean found = false;
			for (String expectedMethod : expected) {
				if (TaggedBlockFinder.isMethodCall(variable, expectedMethod,
						call)) {
					found = true;
					break;
				}
			}
			assertTrue("Dependence chain includes an unexpected method call: "
					+ TaggedBlockFinder.extractMethodCallTarget(call) + "."
					+ TaggedBlockFinder.extractMethodCallName(call), found);
		}
	}

	/**
	 * Searches dependency chain for a method call matching criteria.
	 * 
	 * @param variable
	 *            Expected variable name.
	 * @param method
	 *            Expected method name.
	 * @param chain
	 *            Dependency chain.
	 */
	private void checkCallInChain(String variable, String method,
			Iterable<Call> chain) {
		for (SimpleNode statement : chain) {
			try {
				if (TaggedBlockFinder.isMethodCall(variable, method,
						(Call) statement))
					return;
			} catch (ClassCastException e) {
			}
		}
		assertTrue("Method call '" + method + "'not found in chain: " + chain,
				false);
	}

	private Set<String> result(String... methodNames) {
		return new HashSet<String>(Arrays.asList(methodNames));
	}
}
