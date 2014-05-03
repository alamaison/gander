package uk.ac.ic.doc.gander.cfg;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;

public class BasicBlock extends AbstractCollection<SimpleNode> {

    public interface Visitor {

        void visitBlock(BasicBlock cur, BasicBlock parent);

    }

    public ArrayList<SimpleNode> statements;

    private final Set<BasicBlock> out = new HashSet<BasicBlock>();
    private final Set<BasicBlock> predecessors = new HashSet<BasicBlock>();

    public BasicBlock() {
        this.statements = new ArrayList<SimpleNode>();
    }

    @Override
    public Iterator<SimpleNode> iterator() {
        return statements.iterator();
    }

    public Collection<BasicBlock> getSuccessors() {
        return out;
    }

    public Collection<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public void link(BasicBlock successor) {
        out.add(successor);
        successor.predecessors.add(this);
    }

    public void addStatement(SimpleNode stmt) {
        this.statements.add(stmt);

    }

    @Override
    public boolean isEmpty() {
        return statements.size() == 0;
    }

    /**
     * Once a block has been linked, it is no longer eligible for adding
     * statements to.
     */
    public boolean isClosed() {
        return !getSuccessors().isEmpty();
    }

    @Override
    public String toString() {
        return statements.toString();
    }

    @Override
    public int size() {
        return statements.size();
    }
}
