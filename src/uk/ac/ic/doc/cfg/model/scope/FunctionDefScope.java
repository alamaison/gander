package uk.ac.ic.doc.cfg.model.scope;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class FunctionDefScope extends Scope {

	private BasicBlock start;
	private BasicBlock end;
	private BasicBlock exception = null;
	
	private FunctionDef node;
	private Set<BasicBlock> blocks = new HashSet<BasicBlock>();

	public FunctionDefScope(FunctionDef node) throws Exception {
		super();
		this.node = node;
	}

	public ScopeExits process() throws Exception {

		BodyScope scope = new BodyScope(node.body, null, this);
		ScopeExits body = scope.process();

		start = newBlock();
		if (!body.isEmpty())
			start.link(body.getRoot());

		ScopeExits function = new ScopeExits();
		function.inheritExitsFrom(body);
		function.setRoot(start);
		
		end = newBlock();
		if (!function.isEmpty()) {
			function.linkFallThroughsTo(end);
			function.linkReturnsTo(end);
		} else {
			start.link(end);
		}
		
		if (function.canRaise()) {
			exception = newBlock();
			function.linkRaisesTo(exception);
		}

		return new ScopeExits();
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
		return start;
	}

	public BasicBlock getEnd() {
		return end;
	}
	
	public BasicBlock getException() {
		return exception;
	}
}
