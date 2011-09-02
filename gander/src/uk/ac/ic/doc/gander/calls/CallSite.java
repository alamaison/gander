package uk.ac.ic.doc.gander.calls;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.model.Namespace;

public class CallSite {

	public CallSite(Call call, Namespace scope, BasicBlock block) {
		this.call = call;
		this.scope = scope;
		this.block = block;
	}

	private Call call;
	private Namespace scope;
	private BasicBlock block;

	public Call getCall() {
		return call;		
	}
	
	public Namespace getScope() {
		return scope;
	}
	
	public BasicBlock getBlock() {
		return block;
	}
}
