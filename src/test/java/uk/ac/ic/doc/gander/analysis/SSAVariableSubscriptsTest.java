package uk.ac.ic.doc.gander.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.AbstractTaggedCallTest;
import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.ResourceResolver;
import uk.ac.ic.doc.gander.analysis.ssa.SSAVariableSubscripts;

public class SSAVariableSubscriptsTest extends AbstractTaggedCallTest {

	private static final File TEST_FOLDER = new File(
			"../python_test_code/basic/");
	protected SSAVariableSubscripts renamer;

	public SSAVariableSubscriptsTest() {
		super(ResourceResolver.resolveRelativeToClass(TEST_FOLDER,
				SSAVariableSubscriptsTest.class));
	}

	protected void initialise(String caseName, int expectedBlockCount)
			throws Throwable {
		super.initialise(caseName, expectedBlockCount);
		renamer = new SSAVariableSubscripts(graph);
	}

	@Test
	public void testKillsInSameBlock() throws Throwable {
		initialise("kills_in_same_block", 1);
		String[][] renameGroups = { { "y.a(tag1)", "y.b(tag2)" },
				{ "y.b(tag3)", "y.c(tag4)" } };
		checkRename(renameGroups);
	}

	@Test
	public void testMultikill() throws Throwable {
		initialise("multikill", 1);
		String[][] renameGroups = { { "y.a(tag1)" }, { "y.d(tag4)" },
				{ "x.b(tag2)" }, { "x.c(tag3)" } };
		checkRename(renameGroups);
	}

	@Test
	public void testAssignmentInNonDomBlock() throws Throwable {
		initialise("assignment_in_non_dom_block", 3);
		String[][] renameGroups = { { "y.a(tag1)", "y.b(tag2)" },
				{ "y.e(tag5)" }, { "y.c(tag3)", "y.d(tag4)" } };
		checkRename(renameGroups);
	}

	@Test
	public void testCementFindLoader() throws Throwable {
		initialise("find_loader", 4);
		String[][] renameGroups = {};
		checkRename(renameGroups);
	}

	@Test
	public void testAugAssign() throws Throwable {
		initialise("aug_assign", 1);
		String[][] renameGroups = { { "y.a(tag1)" }, { "y.a(tag2)" } };
		checkRename(renameGroups);
	}

	private void checkRename(String[][] renameGroups) throws Throwable {
		Map<String, Set<Integer>> seenSubscripts = new HashMap<String, Set<Integer>>();
		for (String[] group : renameGroups) {
			Call call = findTaggedStatement(group[0]).getCall();
			Name variable = (Name) CallHelper.indirectCallTarget(call);
			int subscript = renamer.subscript(variable);

			Set<Integer> seen = seenSubscripts.get(variable.id);
			if (seen == null) {
				seen = new HashSet<Integer>();
				seenSubscripts.put(variable.id, seen);
			}
			assertTrue("Duplicate subscript: " + subscript,
					!seen.contains(subscript));
			seen.add(subscript);

			for (int i = 1; i < group.length; ++i) {

				call = findTaggedStatement(group[i]).getCall();
				Name nextVariable = (Name) CallHelper.indirectCallTarget(call);
				assertEquals(
						"Test convention violation: mixed target variables "
								+ "in group", variable.id, nextVariable.id);

				int nextSubscript = renamer.subscript(nextVariable);
				assertEquals("Subscript mismatch within group", subscript,
						nextSubscript);
			}
		}
	}
}
