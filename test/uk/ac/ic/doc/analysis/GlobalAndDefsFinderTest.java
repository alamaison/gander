package uk.ac.ic.doc.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class GlobalAndDefsFinderTest extends AbstractTaggedCallTest {

	private static final String TEST_FOLDER = "python_test_code/matching_dom_length/basic/";
	private GlobalsAndDefsFinder finder;

	public GlobalAndDefsFinderTest() {
		super(TEST_FOLDER);
	}

	protected void initialise(String caseName, int expectedBlockCount)
			throws Throwable {
		super.initialise(caseName, expectedBlockCount);
		finder = new GlobalsAndDefsFinder(graph);
	}

	@Test
	public void testBasic() throws Throwable {
		initialise("basic", 1);
		checkGlobals("x");
		String[][] defs = { { "y", "y.b(tag)" } };
		checkDefs(defs);
	}

	@Test
	public void testKillsInSameBlock() throws Throwable {
		initialise("kills_in_same_block", 1);
		checkGlobals("x", "z");
		String[][] defs = { { "y", "y.a(tag1)" } };
		checkDefs(defs);
	}

	@Test
	public void testMixedKillsInSameBlock() throws Throwable {
		initialise("mixed_kills_in_same_block", 1);
		checkGlobals("x", "y", "z");
		String[][] defs = { { "x", "x.a(tag1)" } };
		checkDefs(defs);
	}

	@Test
	public void testAssignValueIsUse() throws Throwable {
		initialise("assign_value_is_use", 1);
		checkGlobals("x");
		String[][] defs = { { "x", "x.b(tag2)" } };
		checkDefs(defs);
	}

	@Test
	public void testMultikill() throws Throwable {
		initialise("multikill", 1);
		checkGlobals("x", "y");
		String[][] defs = { { "y", "y.a(tag1)" }, { "x", "y.a(tag1)" } };
		checkDefs(defs);
	}

	@Test
	public void testIncludeOnlyDominators() throws Throwable {
		initialise("include_only_dominators", 3);
		checkGlobals("x", "y");
		String[][] defs = {};
		checkDefs(defs);
	}

	@Test
	public void testIncludeOnlyPostdominators() throws Throwable {
		initialise("include_only_postdominators", 3);
		checkGlobals("x", "y");
		String[][] defs = {};
		checkDefs(defs);
	}

	@Test
	public void testIncludeDomAndPostdom() throws Throwable {
		initialise("include_dom_and_postdom", 6);
		checkGlobals("x", "y", "a", "z");
		String[][] defs = {};
		checkDefs(defs);
	}

	@Test
	public void testAssignmentInNonDomBlock() throws Throwable {
		initialise("assignment_in_non_dom_block", 3);
		checkGlobals("x", "y", "rob", "bob");
		String[][] defs = { { "y", "y.a(tag1)", "y.e(tag5)" },
				{ "x", "y.a(tag1)" } };
		checkDefs(defs);
	}

	/**
	 * The RHS of an assignment must be considered before the left.
	 * 
	 * Although x is assigned to in this test case, it if also used on the LHS
	 * of the assignment. This use executes before the kill, therefore the use
	 * puts x in the global set.
	 */
	@Test
	public void testAssignmentConsideredInCorrectOrder() throws Throwable {
		initialise("assignment_considered_in_correct_order", 1);
		checkGlobals("x");
		String[][] defs = { { "x", "x.a(tag1)" } };
		checkDefs(defs);
	}

	private void checkDefs(String[][] expectedDefs) throws Throwable {
		Set<String> expectedDefinitions = new HashSet<String>();
		for (String[] defDescriptor : expectedDefs) {
			assert defDescriptor.length > 1;

			String variable = defDescriptor[0];
			Iterable<BasicBlock> locations = finder.definingLocations(variable);
			assertTrue("Variable '" + variable + "' not defined",
					locations != null);
			expectedDefinitions.add(variable);

			Set<BasicBlock> expectedLocations = new HashSet<BasicBlock>();
			for (int i = 1; i < defDescriptor.length; ++i) {
				expectedLocations.add(findTaggedBlock(defDescriptor[i]));
			}
			assertEquals(expectedLocations, locations);
		}
		assertEquals(expectedDefinitions, finder.definitions());
	}

	private void checkGlobals(String... expectedGlobal) throws Exception {
		assertEquals(new HashSet<String>(Arrays.asList(expectedGlobal)),
				finder.globals());
	}
}
