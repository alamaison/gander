package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.TerminatorBasicBlock;

public class FunctionDefScope extends Scope {

	private Statement start;
	private Statement end;
	private Statement exception;

	private FunctionDef node;
	private Set<BasicBlock> blocks = new HashSet<BasicBlock>();

	public FunctionDefScope(FunctionDef node) throws Exception {
		super();
		this.node = node;
	}

	public Statement process() throws Exception {

		start = new EmptyScope(this, newTerminatorBlock()).process();
		end = new EmptyScope(this, newTerminatorBlock()).process();
		exception = new EmptyScope(this, newTerminatorBlock()).process();

		Statement body = new BodyScope(node.body, start, start.fallthroughs(),
				this).process();

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
