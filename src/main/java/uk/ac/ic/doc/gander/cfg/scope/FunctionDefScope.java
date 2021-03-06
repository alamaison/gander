package uk.ac.ic.doc.gander.cfg.scope;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.TerminatorBasicBlock;

public class FunctionDefScope extends Scope {

    private Statement start;
    private Statement end;
    private Statement exception;

    private FunctionDef node;
    private Set<BasicBlock> blocks = new HashSet<BasicBlock>();

    public FunctionDefScope(FunctionDef node) {
        super();
        this.node = node;
    }

    public Statement process() {

        start = new EmptyScope(this, newTerminatorBlock()).process();
        end = new EmptyScope(this, newTerminatorBlock()).process();
        exception = new EmptyScope(this, newTerminatorBlock()).process();

        Statement body = buildGraphForceNewBlock(node.body, start,
                start.fallthroughs());

        body.linkFallThroughsTo(end);
        body.linkReturnsTo(end);
        body.linkRaisesTo(exception);

        return new Statement();
    }

    private BasicBlock newTerminatorBlock() {
        BasicBlock b = new TerminatorBasicBlock();
        blocks.add(b);
        return b;
    }

    protected BasicBlock newBlock() {
        BasicBlock b = new BasicBlock();
        blocks.add(b);
        return b;
    }

    public Set<BasicBlock> getBlocks() {
        return blocks;
    }

    public BasicBlock getStart() {
        return start.uniqueFallthrough();
    }

    public BasicBlock getEnd() {
        return end.uniqueFallthrough();
    }

    public BasicBlock getException() {
        return exception.uniqueFallthrough();
    }
}
