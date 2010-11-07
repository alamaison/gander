package uk.ac.ic.doc.cfg.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.python.pydev.parser.jython.ast.stmtType;

public class BasicBlock implements Iterable<stmtType> {
	public ArrayList<stmtType> statements;

	private Set<BasicBlock> out = new HashSet<BasicBlock>();
	
	BasicBlock() {
		this.statements = new ArrayList<stmtType>();
	}
	
	BasicBlock(Collection<? extends stmtType> stmts) {
		this.statements = new ArrayList<stmtType>(statements);
	}

	public BasicBlock(stmtType[] stmts) {
		this.statements = new ArrayList<stmtType>(Arrays.asList(stmts));
	}

	public Iterator<stmtType> iterator() {
		return statements.iterator();
	}

	public Set<BasicBlock> getOutSet() {
		return out;
	}

	public void link(BasicBlock successor) {
		out.add(successor);
	}
	
	public void addStatement(stmtType stmt) {
		this.statements.add(stmt);
			
	}

	public boolean isEmpty() {
		return statements.size() == 0;
	}
}
