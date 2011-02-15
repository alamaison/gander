package uk.ac.ic.doc.analysis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import uk.ac.ic.doc.analysis.dominance.DominationLength;
import uk.ac.ic.doc.cfg.Model;

public class DominationLengthTest {

	private static final String LENGTH_PROJ = "python_test_code/dom_length";
	private static final String LENGTH_PKG_PROJ =
		"python_test_code/dom_length_package";

	private DominationLength analyser;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	public void initialise(String project) throws Throwable {
		Model model = createTestModel(project);

		analyser = new DominationLength(model);
	}

	@Test
	public void testDomExpressionCount() throws Throwable {
		initialise(LENGTH_PROJ);
		assertEquals(12, analyser.all.expressionCount());
	}

	@Test
	public void testDomLengthMax() throws Throwable {
		initialise(LENGTH_PROJ);
		assertEquals(8, analyser.all.max());
	}

	@Test
	public void testDomLengthMin() throws Throwable {
		initialise(LENGTH_PROJ);
		assertEquals(1, analyser.all.min());
	}

	@Test
	public void testDomLengthAvg() throws Throwable {
		initialise(LENGTH_PROJ);
		assertEquals(5.583, analyser.all.average(), 0.0006);
	}

	@Test
	public void testDomPkgExpressionCount() throws Throwable {
		initialise(LENGTH_PKG_PROJ);
		assertEquals(12, analyser.all.expressionCount());
	}

	@Test
	public void testDomPkgLengthMax() throws Throwable {
		initialise(LENGTH_PKG_PROJ);
		assertEquals(8, analyser.all.max());
	}

	@Test
	public void testDomPkgLengthMin() throws Throwable {
		initialise(LENGTH_PKG_PROJ);
		assertEquals(1, analyser.all.min());
	}

	@Test
	public void testDomPkgLengthAvg() throws Throwable {
		initialise(LENGTH_PKG_PROJ);
		assertEquals(5.583, analyser.all.average(), 0.0006);
	}
}
