package uk.ac.ic.doc.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class PhiPlacementTest extends AbstractTaggedCallTest {

	private static final String TEST_FOLDER = "python_test_code/matching_dom_length/basic/";
	protected PhiPlacement phi;

	public PhiPlacementTest() {
		super(TEST_FOLDER);
	}

	protected void initialise(String caseName, int expectedBlockCount)
			throws Throwable {
		super.initialise(caseName, expectedBlockCount);
		phi = new PhiPlacement(graph);
	}

	@Test
	public void testAssignmentInNonDomBlock() throws Throwable {
		initialise("assignment_in_non_dom_block", 3);
		String[][] phiAt = { { "y.c(tag3)", "y" } };
		checkPhi(phiAt);
	}

	@Test
	public void testMultikill() throws Throwable {
		initialise("multikill", 1);
		String[][] phiAt = {};
		checkPhi(phiAt);
	}

	private void checkPhi(String[][] expectedPhis) throws Throwable {
		Set<BasicBlock> blocksWithPhiNodes = new HashSet<BasicBlock>();
		for (String[] phiDescriptor : expectedPhis) {
			assert phiDescriptor.length > 1;

			String taggedCall = phiDescriptor[0];
			BasicBlock location = findTaggedBlock(taggedCall);
			Iterable<String> targets = phi.phiTargets(location);
			assertTrue("No phi-functions in node containing '" + taggedCall
					+ "'", targets != null);
			blocksWithPhiNodes.add(location); // for absence-testing later

			Set<String> expectedTargets = new HashSet<String>();
			for (int i = 1; i < phiDescriptor.length; ++i) {
				expectedTargets.add(phiDescriptor[i]);
			}
			assertEquals(expectedTargets, targets);
		}

		// Absence test
		Set<BasicBlock> blocksWhichShouldntHavePhis = new HashSet<BasicBlock>(
				graph.getBlocks());
		blocksWhichShouldntHavePhis.removeAll(blocksWithPhiNodes);
		for (BasicBlock block : blocksWhichShouldntHavePhis) {
			Iterable<String> targets = phi.phiTargets(block);
			assertTrue("Unexpected phi node in block: " + block,
					targets == null);
		}

	}
}
