package uk.ac.ic.doc.analysis;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.analysis.dominance.Domination;
import uk.ac.ic.doc.analysis.dominance.Postdomination;
import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

public class DependenceChain {

	private class SubscriptMatcher extends BasicBlockVisitor {

		private Set<SimpleNode> calls = new HashSet<SimpleNode>();
		private Name target;
		private int subscript;

		public SubscriptMatcher(BasicBlock containingBlock, Name target,
				int subscript) throws Exception {
			this.target = target;
			this.subscript = subscript;
			for (SimpleNode node : containingBlock) {
				node.accept(this);
			}

		}

		@Override
		public Object visitCall(Call node) throws Exception {
			try {
				Name callTarget = extractMethodCallTarget(node);
				if (callTarget.id.equals(target.id)
						&& renamer.subscript(callTarget) == subscript)
					calls.add(node);
			} catch (ClassCastException e) {
			}
			return null;
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		Set<SimpleNode> matchingCalls() {
			return calls;
		}
	}

	private Cfg graph;
	private VariableRenaming renamer;

	public DependenceChain(Cfg graph) {
		this.graph = graph;
	}

	static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	public Iterable<SimpleNode> dependentStatements(Call call,
			BasicBlock containingBlock) throws Exception {

		Name target = extractMethodCallTarget(call);
		renamer = new VariableRenaming(graph);
		int permittedSubscript = renamer.subscript(target);

		Set<SimpleNode> dependentCalls = new HashSet<SimpleNode>();

		SubscriptMatcher matcher = new SubscriptMatcher(containingBlock,
				target, permittedSubscript);
		dependentCalls.addAll(matcher.matchingCalls());

		Domination domAnalyser = new Domination(renamer.getDomInfo());
		Postdomination postdomAnalyser = new Postdomination(graph);

		for (BasicBlock dominator : domAnalyser.dominators(containingBlock)) {
			if (dominator == containingBlock)
				continue;
			matcher = new SubscriptMatcher(dominator, target,
					permittedSubscript);
			dependentCalls.addAll(matcher.matchingCalls());
		}

		for (BasicBlock postdominator : postdomAnalyser
				.dominators(containingBlock)) {
			if (postdominator == containingBlock)
				continue;
			matcher = new SubscriptMatcher(postdominator, target,
					permittedSubscript);
			dependentCalls.addAll(matcher.matchingCalls());
		}

		return dependentCalls;
	}
}
