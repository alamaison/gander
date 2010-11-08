package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.If;

public class IfScope extends Scope {

	private BasicBlock thenBlock = null;
	private BasicBlock elseBlock = null;
	
	private boolean nesting = false;
	
	private Scope parent;
		
	public IfScope(If node, Scope parent) throws Exception {
		super();
		this.parent = parent;
		node.accept(this);
	}

	@Override
	public Object visitIf(If node) throws Exception {
		
		if (nesting) {
			IfScope scope = new IfScope(node, this);
		}
		else
		{
			nesting = true;
			
			// This code is take from If.traverse rather than allowing it to
			// traverse itself as I can't think of a way to distinguish
			// which branch we are in.  Although the else branch is held
			// in a 'suite' node in the AST, it doesn't call visitSuite on the
			// visitor.
			
			setCurrentBlock(this.parent.getCurrentBlock());
			
	        if (node.test != null){
	            node.test.accept(this);
	        }
	        
			if (node.orelse == null) {
				parent.fallthrough(getCurrentBlock());
			}
	        
	        if (node.body != null) {
	        	thenBlock = new BasicBlock();
	        	setCurrentBlock(thenBlock);
	        	
	            for (int i = 0; i < node.body.length; i++) {
	                if (node.body[i] != null){
	                    node.body[i].accept(this);
	                }
	            }
	        }
	        
	        if (node.orelse != null){
				elseBlock = new BasicBlock();
				setCurrentBlock(elseBlock);
	            node.orelse.accept(this);
	        }
	        
			parent.linkAfterCurrent(thenBlock);
			parent.fallthrough(thenBlock);
			if (elseBlock != null) {
				parent.linkAfterCurrent(elseBlock);
				parent.fallthrough(elseBlock);
			}
		}
		
		return null;
	}

	@Override
	public void fallthrough(BasicBlock block) {
		parent.fallthrough(block);
	}

}
