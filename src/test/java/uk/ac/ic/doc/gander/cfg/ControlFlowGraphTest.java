package uk.ac.ic.doc.gander.cfg;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.AbstractTaggedGraphTest;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;

class ControlFlowGraphTest extends AbstractTaggedGraphTest {

    public ControlFlowGraphTest(String[][] links, Cfg graph) {
        super(links, graph, "Control-flow");
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