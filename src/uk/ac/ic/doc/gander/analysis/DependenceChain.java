package uk.ac.ic.doc.gander.analysis;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.analysis.dominance.Domination;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.cfg.model.Module;
import uk.ac.ic.doc.gander.flowinference.types.TypeResolutionVisitor;

public class DependenceChain {

	private class SubscriptMatcher extends BasicBlockVisitor {

		private Set<Call> calls = new HashSet<Call>();
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

		Set<Call> matchingCalls() {
			return calls;
		}
	}

	private Cfg graph;
	private VariableRenaming renamer;
	private Module module;

	public DependenceChain(Module module, Cfg graph) {
		this.module = module;
		this.graph = graph;
	}

	static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	/**
	 * Given a call, return all calls that target the same variable that
	 * are control-dependent on the original call.
	 */
	public Set<Call> dependentCalls(Call call,
			BasicBlock containingBlock) throws Exception {
		
		Name target = extractMethodCallTarget(call);
		
		TypeResolutionVisitor typer = new TypeResolutionVisitor(module.getAst());
		if (typer.typeOf(target.id) instanceof uk.ac.ic.doc.gander.flowinference.types.Module)
			return null;

		renamer = new VariableRenaming(graph);
		int permittedSubscript = renamer.subscript(target);

		Set<Call> dependentCalls = new HashSet<Call>();

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
