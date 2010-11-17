package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.While;

public class WhileScope extends Scope {
	
	private FunctionDefScope parent;

	public WhileScope(While node, FunctionDefScope parent) throws Exception {
		super();
		this.parent = parent;
		node.accept(this);
	}

	@Override
	public Object visitWhile(While node) throws Exception {
		
		boolean isParentEmpty = parent.getCurrentBlock().isEmpty();
		
		BasicBlock testBlock;
		if (isParentEmpty)
			testBlock = parent.getCurrentBlock();
		else
			testBlock = new BasicBlock();
		
		BasicBlock bodyBlock = new BasicBlock();
		
		setCurrentBlock(testBlock);

		if (node.test != null){
            node.test.accept(this);
        }
		
		setCurrentBlock(bodyBlock);
        if (node.body != null) {
            for (int i = 0; i < node.body.length; i++) {
                if (node.body[i] != null){
                    node.body[i].accept(this);
                }
            }
        }
        
        // TODO Handle Python while loops that have 'else' clauses!
//        if (node.orelse != null){
//            node.orelse.accept(this);
//        }
        
        if (!isParentEmpty)
        	parent.linkAfterCurrent(testBlock);

        testBlock.link(bodyBlock);
        bodyBlock.link(testBlock);
        parent.fallthrough(testBlock);
        
        return null;
	}

	@Override
	protected void fallthrough(BasicBlock block) {
		parent.fallthrough(block);
	}
}
