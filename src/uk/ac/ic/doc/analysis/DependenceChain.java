package uk.ac.ic.doc.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.analysis.dominance.DomFront.DomInfo;
import uk.ac.ic.doc.analysis.dominance.Domination;
import uk.ac.ic.doc.analysis.dominance.Postdomination;
import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

public class DependenceChain {

	public class SubscriptMatcher extends BasicBlockVisitor {

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

	public class StoreCollector extends VisitorBase {

		private Collection<Name> names = new Stack<Name>();

		public StoreCollector(SimpleNode target) throws Exception {
			target.accept(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		@Override
		public Object visitName(Name node) throws Exception {
			names.add(node);
			return null;
		}

		public Iterable<Name> getNames() {
			return names;
		}

	}

	public class DefUseGrouper extends VisitorBase {

		private class DefUseData {

			private class UseGroups {

				private Collection<Iterable<SimpleNode>> groups = new ArrayList<Iterable<SimpleNode>>();
				private Collection<SimpleNode> currentGroup = null;

				void addToCurrentGroup(SimpleNode node) {
					if (currentGroup == null) {
						currentGroup = new ArrayList<SimpleNode>();
						groups.add(currentGroup);
					}

					currentGroup.add(node);
				}

				void kill() {
					currentGroup = null;
				}

				Iterable<Iterable<SimpleNode>> getGroups() {
					return groups;
				}
			}

			private Map<String, UseGroups> targetGroups = new HashMap<String, UseGroups>();

			public void addUse(Name target, SimpleNode statement) {
				if (!targetGroups.containsKey(target.id)) {
					targetGroups.put(target.id, new UseGroups());
				}

				targetGroups.get(target.id).addToCurrentGroup(statement);
			}

			public void kill(Name target) {
				UseGroups uses = targetGroups.get(target.id);
				if (uses != null)
					uses.kill();
			}

			public Iterable<Iterable<SimpleNode>> groupsTargetting(Name target) {

				return targetGroups.get(target.id).getGroups();
			}
		}

		private DefUseData data = new DefUseData();

		public DefUseGrouper(List<SimpleNode> statements) throws Exception {
			for (SimpleNode statement : statements) {
				statement.accept(this);
			}
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		@Override
		public Object visitCall(Call node) throws Exception {
			data.addUse(extractMethodCallTarget(node), node);
			return null;
		}

		@Override
		public Object visitAssign(Assign node) throws Exception {

			// The RHS of an assignment may be a variable use just like any
			// other
			node.value.accept(this);

			for (SimpleNode target : node.targets) {
				for (Name name : new StoreCollector(target).getNames()) {
					data.kill(name);
				}
			}

			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		public Iterable<SimpleNode> group(Call call) {
			Iterable<Iterable<SimpleNode>> groups = data
					.groupsTargetting(extractMethodCallTarget(call));
			for (Iterable<SimpleNode> group : groups) {
				for (SimpleNode node : group) {
					if (node.equals(call))
						return group;
				}
			}
			return null;
		}

		private Name extractMethodCallTarget(Call call) {
			Attribute fieldAccess = (Attribute) call.func;
			return (Name) fieldAccess.value;
		}
	}

	public class NodeExpander {

		private class NodeExpanderVisitor extends VisitorBase {

			private Collection<SimpleNode> statements = new ArrayList<SimpleNode>();

			NodeExpanderVisitor(SimpleNode node) throws Exception {
				node.accept(this);
			}

			@Override
			public Object visitCall(Call node) throws Exception {
				if (node.func instanceof Attribute) // only add method calls
					statements.add(node);
				return null;
			}

			@Override
			public Object visitAssign(Assign node) throws Exception {
				statements.add(node);
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

			public Collection<SimpleNode> statements() {
				return statements;
			}

		}

		public List<SimpleNode> filter(Iterable<SimpleNode> statements)
				throws Exception {
			List<SimpleNode> filteredStatements = new ArrayList<SimpleNode>();
			for (SimpleNode node : statements) {
				NodeExpanderVisitor visitor = new NodeExpanderVisitor(node);
				filteredStatements.addAll(visitor.statements());
			}

			return filteredStatements;
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
		// return findStatementGroup(call,
		// expandStatements(controlDependentStatements));
	}

	private Iterable<SimpleNode> findStatementGroup(Call call,
			List<SimpleNode> statements) throws Exception {
		DefUseGrouper grouper = new DefUseGrouper(statements);
		return grouper.group(call);
	}

	private List<SimpleNode> expandStatements(
			List<SimpleNode> controlDependentStatements) throws Exception {
		NodeExpander filter = new NodeExpander();
		return filter.filter(controlDependentStatements);
	}

}
