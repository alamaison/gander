package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Method;

public class ClassTest {

	private class ControlFlowGraphTest extends AbstractTaggedGraphTest {

		public ControlFlowGraphTest(String[][] links, Set<BasicBlock> blocks) {
			super(links, blocks, "Control-flow");
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return source.getSuccessors().contains(target);
		}

		@Override
		protected boolean selfLinkRequired() {
			return false;
		}

		@Override
		protected Set<BasicBlock> getLinkToAllBlocks() {
			return new HashSet<BasicBlock>();
		}
	}

	private static final String CONTROL_FLOW_PROJ = "python_test_code/control_flow";

	private Cfg graph;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	public void initialise(String className, String methodName)
			throws Throwable, Exception {
		Model model = createTestModel(CONTROL_FLOW_PROJ);
		Method method = model.getTopLevelPackage().getModules().get("classes")
				.getClasses().get(className).getMethods().get(methodName);
		assertTrue("No function " + methodName, method != null);

		graph = method.getCfg();
	}

	private void checkControlFlow(String[][] dominators) {

		Set<BasicBlock> allBlocks = graph.getBlocks();

		new ControlFlowGraphTest(dominators, allBlocks).run();
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