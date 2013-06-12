package uk.ac.ic.doc.gander.analysis.ssa;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.analysis.BasicBlockTraverser;
import uk.ac.ic.doc.gander.cfg.BasicBlock;

public class NameExtractor {

	private List<Name> names = new ArrayList<Name>();

	NameExtractor(BasicBlock block) {
		for (SimpleNode statement : block) {
			try {
				statement.accept(new NameExtractorVisitor());
			} catch (Exception e) {
				// No checked exceptions thrown in NameExtractorVisitor
				throw new RuntimeException(e);
			}
		}
	}

	List<Name> operations() {
		return names;
	}

	private final class NameExtractorVisitor extends BasicBlockTraverser {

		/**
		 * Custom-traverse Assign nodes.
		 * 
		 * The default traversal visits the target before the value. NOT what we
		 * want.
		 */
		@Override
		public Object visitAssign(Assign node) throws Exception {
			if (node.value != null) {
				node.value.accept(this);
			}
			if (node.targets != null) {
				for (SimpleNode target : node.targets) {
					if (target != null) {
						target.accept(this);
					}
				}
			}
			return null;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			names.add(node);
			return null;
		}
	}
}
