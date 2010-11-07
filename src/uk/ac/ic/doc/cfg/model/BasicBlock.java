package uk.ac.ic.doc.cfg.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;

public class BasicBlock implements Iterable<SimpleNode> {
	public ArrayList<SimpleNode> statements;

	private Set<BasicBlock> out = new HashSet<BasicBlock>();
	
	BasicBlock() {
		this.statements = new ArrayList<SimpleNode>();
	}
	
	BasicBlock(Collection<? extends SimpleNode> stmts) {
		this.statements = new ArrayList<SimpleNode>(statements);
	}

	public BasicBlock(SimpleNode[] stmts) {
		this.statements = new ArrayList<SimpleNode>(Arrays.asList(stmts));
	}

	public Iterator<SimpleNode> iterator() {
		return statements.iterator();
	}

	public Set<BasicBlock> getOutSet() {
		return out;
	}

	public void link(BasicBlock successor) {
		out.add(successor);
	}
	
	public void addStatement(SimpleNode stmt) {
		this.statements.add(stmt);
			
	}

	public boolean isEmpty() {
		return statements.size() == 0;
	}
}
