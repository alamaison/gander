package uk.ac.ic.doc.gander.analysis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import uk.ac.ic.doc.gander.cfg.Model;
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.cfg.model.Module;

public abstract class AbstractTaggedCallTest {

	protected Cfg graph;
	protected TaggedBlockFinder tagFinder;
	private final String projectDirectory;
	protected Model model;
	protected Module module;

	public AbstractTaggedCallTest(String projectDirectory) {
		this.projectDirectory = projectDirectory;
	}

	protected void initialise(String caseName) throws Throwable {
		URL topLevel = getClass().getResource(projectDirectory);

		File topLevelDirectory = new File(topLevel.toURI());

		model = new Model(topLevelDirectory);
		module = model.getTopLevelPackage().getModules()
				.get("case");
		graph = module.getFunctions().get(caseName).getCfg();

		tagFinder = new TaggedBlockFinder(graph);
	}

	protected void initialise(String caseName, int expectedBlockCount)
			throws Throwable {
		initialise(caseName);
		checkBlockCount(expectedBlockCount);
	}

	protected Statement findTaggedStatement(String taggedCall) throws Exception {
		return tagFinder.findTaggedStatement(taggedCall);
	}

	protected BasicBlock findTaggedBlock(String taggedCall) throws Exception {
		return findTaggedStatement(taggedCall).getBlock();
	}

	/**
	 * Test number of blocks in graph.
	 * 
	 * @param expected
	 *            Number of <b>operative</b> blocks, i.e., excluding START, END
	 *            and EXCEPTION.
	 */
	private void checkBlockCount(int expected) {
		assertEquals(expected, graph.getBlocks().size() - 3);
	}

	protected String variableFromTag(String taggedCall) {
		return tagFinder.variableFromTag(taggedCall);
	}

	protected String methodFromTag(String taggedCall) {
		return tagFinder.methodFromTag(taggedCall);
	}

	protected String tagFromTag(String taggedCall) {
		return tagFinder.tagFromTag(taggedCall);
	}
}