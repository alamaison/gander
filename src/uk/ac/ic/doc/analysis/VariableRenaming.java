package uk.ac.ic.doc.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.analysis.dominance.DomFront.DomInfo;
import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

/**
 * Based on Figure 9.13, p169 Engineering a Compiler.
 */
public class VariableRenaming {

	private Map<Name, Integer> subscripts = new HashMap<Name, Integer>();
	private Map<String, Integer> counters = new HashMap<String, Integer>();
	private Map<String, Stack<Integer>> stacks = new HashMap<String, Stack<Integer>>();
	private Map<BasicBlock, Set<BasicBlock>> domSuccessors = new HashMap<BasicBlock, Set<BasicBlock>>();
	private PhiPlacement phis;

	public VariableRenaming(Cfg graph) throws Exception {
		phis = new PhiPlacement(graph);
		buildDomTree(graph);

		rename(graph.getStart());
	}

	private void push(String variable, int subscript) {
		Stack<Integer> stack = stacks.get(variable);
		if (stack == null) {
			stack = new Stack<Integer>();
			stacks.put(variable, stack);
		}
		stack.push(subscript);
	}

	private int top(String variable) {
		Stack<Integer> stack = stacks.get(variable);
		if (stack == null || stack.isEmpty())
			return -1;
		
		return stack.peek();
	}

	private void rename(BasicBlock node) throws Exception {
		Iterable<String> targets = phis.phiTargets(node);
		if (targets != null) {
			for (String target : targets) {
				newName(target);
			}
		}

		DefUseSeparator defUses = new DefUseSeparator(node);
		for (IDefUse op : defUses.operations()) {
			Name name = op.getName();
			if (op instanceof Use) {
				Integer prev = subscripts.put(name, top(name.id));
				assert prev == null;
			} else {
				assert op instanceof Def;
				Integer prev = subscripts.put(name, newName(name.id));
				assert prev == null;
			}
		}

		Set<BasicBlock> successors = domSuccessors.get(node);
		if (successors != null) {
			for (BasicBlock successor : successors) {
				rename(successor);
			}
		}

		if (targets != null) {
			for (String target : targets) {
				stacks.get(target).pop();
			}
		}
		for (IDefUse op : defUses.operations()) {
			String name = op.getName().id;
				if (op instanceof Def) {
				stacks.get(name).pop();
			}
		}
	}

	private int newName(String variable) {
		Integer i = counters.get(variable);
		if (i == null)
			i = new Integer(0);

		counters.put(variable, i + 1);

		push(variable, i);
		return i;
	}

	public int subscript(Name variable) {
		Integer i = subscripts.get(variable);
		if (i == null) // non-global
			return -1;
		else
			return i;
	}

	private void buildDomTree(Cfg graph) {

		Map<BasicBlock, DomInfo> domInfo = phis.getDomInfo();
		for (BasicBlock block : graph.getBlocks()) {
			DomInfo info = domInfo.get(block);

			if (info.idom == null || info.idom == block)
				continue;

			Set<BasicBlock> dominatedChildren = domSuccessors.get(info.idom);
			if (dominatedChildren == null) {
				dominatedChildren = new HashSet<BasicBlock>();
				domSuccessors.put(info.idom, dominatedChildren);
			}
			dominatedChildren.add(block);
		}
	}

	public Map<BasicBlock, DomInfo> getDomInfo() {
		return phis.getDomInfo();
	}
}
