package uk.ac.ic.doc.analysis;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class Statement {

	private Call call;
	private BasicBlock block;

	public Statement(Call node, BasicBlock block) {
		this.call = node;
		this.block = block;
	}

	public Call getCall() {
		return call;
	}

	public BasicBlock getBlock() {
		return block;
	}

}
