/**
 * 
 */
package uk.ac.ic.doc.gander.analysis;

import java.util.ArrayList;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.cfg.BasicBlock;

public class MethodFinder {

	private ArrayList<Call> calls = new ArrayList<Call>();

	public MethodFinder(BasicBlock block) {

		for (SimpleNode statement : block)
			try {
				statement.accept(new MethodFinderVisitor());
			} catch (Exception e) {
				// The visitCall method can't throw an exception so if we
				// get an exceptions here, something has gone very wrong
				throw new RuntimeException(e);
			}
	}

	public Iterable<Call> calls() {
		return calls;
	}

	private class MethodFinderVisitor extends BasicBlockTraverser {

		@Override
		public Object visitCall(Call node) throws Exception {
			if (node.func instanceof Attribute)
				calls.add(node);
			node.traverse(this);
			return null;
		}
	}
}