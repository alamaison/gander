package uk.ac.ic.doc.gander.cfg;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;

public class ClassTest {

	private static final String CONTROL_FLOW_PROJ = "python_test_code/control_flow";

	private Cfg graph;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());
		Hierarchy hierarchy = new Hierarchy(topLevelDirectory);
		Model model = new Model(hierarchy);
		return model;
	}

	public void initialise(String className, String methodName)
			throws Throwable, Exception {
		Model model = createTestModel(CONTROL_FLOW_PROJ);
		Function method = model.loadModule("classes").getClasses().get(
				className).getFunctions().get(methodName);
		assertTrue("No function " + methodName, method != null);

		graph = method.getCfg();
	}

	private void checkControlFlow(String[][] dominators) {
		new ControlFlowGraphTest(dominators, graph).run();
	}

	@Test
	public void testCfg() throws Throwable {
		initialise("test_class", "something");

		String[][] graph = { { "START", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIf() throws Throwable {
		initialise("test_oldstyle_class", "anotherthing");

		String[][] graph = { { "START", "b" }, { "b", "END" } };
		checkControlFlow(graph);
	}

}