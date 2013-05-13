package uk.ac.ic.doc.gander.analysers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;

public class CodeCompletionPredictionTest {

	private static final String TEST_FOLDER = "python_test_code/code_completion";
	private CodeCompletionPrediction analysis;

	@Test
	public void basic() throws Throwable {
		setup("basic");

		checkInterfaceSuccessRate(50.0);
		checkFlowSuccessRate(100.0);
		checkContraSuccessRate(100.0);
	}

	@Test
	public void poly() throws Throwable {
		setup("poly");

		checkInterfaceSuccessRate(50.0);
		checkFlowSuccessRate(100.0);
		checkContraSuccessRate(100.0);
	}

	/**
	 * Regular flow analysis should be imprecise for this one and
	 * contraindication should fix it.
	 */
	@Test
	public void polyImpreciseHelp() throws Throwable {
		setup("poly_imprecise_help");

		checkInterfaceSuccessRate(50.0);
		checkFlowSuccessRate(50.0);
		checkContraSuccessRate(5.0*100/6);
	}

	/**
	 * Regular flow analysis should be imprecise for this one but
	 * contraindication is no use in this situation.
	 */
	@Test
	public void polyImpreciseNoHelp() throws Throwable {
		setup("poly_imprecise_no_help");

		checkInterfaceSuccessRate(0.0);
		checkFlowSuccessRate(50.0);
		checkContraSuccessRate(50.0);
	}

	private void checkInterfaceSuccessRate(double expected) {
		float successRate = analysis.interfaceResult();
		assertEquals(expected, successRate, 0.001);
	}

	private void checkFlowSuccessRate(double expected) {
		float successRate = analysis.flowResult();
		assertEquals(expected, successRate, 0.001);
	}

	private void checkContraSuccessRate(double expected) {
		float successRate = analysis.contraindicationResult();
		assertEquals(expected, successRate, 0.001);
	}

	public void setup(String caseName) throws Throwable {
		URL testFolder = getClass().getResource(TEST_FOLDER);
		File topLevel = new File(new File(testFolder.toURI()), caseName);

		Hierarchy hierarchy = HierarchyFactory.createHierarchy(topLevel);

		analysis = new CodeCompletionPrediction(hierarchy, topLevel);
	}

}
