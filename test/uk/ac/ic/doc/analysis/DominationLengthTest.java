package uk.ac.ic.doc.analysis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ic.doc.cfg.Model;

public class DominationLengthTest {

	private static final String LENGTH_PROJ = "python_test_code/dom_length";

	private DominationLength analyser;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	@Before
	public void initialise() throws Throwable {
		Model model = createTestModel(LENGTH_PROJ);

		analyser = new DominationLength(model);
	}
	
	@Test
	public void testDomExpressionCount() throws Throwable {
		assertEquals(12, analyser.expressionCount());
	}

	@Test
	public void testDomLengthMax() throws Throwable {
		assertEquals(8, analyser.max());
	}

	@Test
	public void testDomLengthMin() throws Throwable {
		assertEquals(1, analyser.min());
	}

	@Test
	public void testDomLengthAvg() throws Throwable {
		assertEquals(5.583, analyser.average(), 0.0006);
	}
}
