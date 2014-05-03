package uk.ac.ic.doc.gander;

import static org.junit.Assert.assertEquals;

import java.io.File;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public abstract class AbstractTaggedCallTest {

    protected Cfg graph;
    protected TaggedBlockFinder tagFinder;
    private final File projectDirectory;
    protected MutableModel model;
    protected Module module;
    protected Function function;

    public AbstractTaggedCallTest(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    protected void initialise(String caseName) throws Throwable {

        Hierarchy hierarchy = HierarchyFactory
                .createHierarchy(projectDirectory);
        model = new DefaultModel(hierarchy);
        module = model.loadModule(moduleToLoad());
        function = module.getFunctions().get(caseName);
        graph = function.getCfg();

        tagFinder = new TaggedBlockFinder(graph);
    }

    protected String moduleToLoad() {
        return "case";
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