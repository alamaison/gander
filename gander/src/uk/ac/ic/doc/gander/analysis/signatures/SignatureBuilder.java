package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.analysis.dominance.Domination;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.OldNamespace;

public class SignatureBuilder {

	/**
	 * Build a signature for the given variable by looking at the given blocks
	 * and recursing into any calls they make.
	 * 
	 * Signatures are in the form of a set of all calls that must be executed in
	 * every case on the object held in the variable. This is the set of calls
	 * that are control-dependent on the given name and operate not only on the
	 * same name but on the same SSA renaming of the name. This ensures that
	 * calls which may happen after re-assigning to a variable aren't included.
	 * 
	 * @param excludeCurrentFeature
	 */
	public Set<Call> signature(Name variable, BasicBlock containingBlock,
			OldNamespace enclosingScope, TypeResolver resolver,
			boolean includeRequiredFeatures, boolean includeFstr,
			boolean excludeCurrentFeature) {

		Set<SimpleNode> nodes = contraindicatingNodes(enclosingScope.getCfg(),
				includeRequiredFeatures, includeFstr, excludeCurrentFeature,
				variable, containingBlock);

		Set<Call> calls = new PartialSignatureFromUsingVariable()
				.buildSignature(variable, nodes, enclosingScope.getCfg());

		calls.addAll(new PartialSignatureFromPassingVariable().buildSignature(
				variable.id, nodes, enclosingScope, resolver));
		return calls;
	}

	/**
	 * Return the correct node for the variant of the analysis we are running.
	 * 
	 * Strict dominators for FSTR; no-strict postdominators for Require Feature
	 * analysis. Both for best results.
	 * 
	 * @param excludeCurrentFeature
	 */
	private Set<SimpleNode> contraindicatingNodes(Cfg graph,
			boolean includeRequiredFeatures, boolean includeFstr,
			boolean excludeCurrentFeature, Name variable,
			BasicBlock containingBlock) {

		Set<SimpleNode> nodes = new HashSet<SimpleNode>();

		if (includeRequiredFeatures) {
			Postdomination postdom = new Postdomination(graph);
			for (BasicBlock postdominatingBlock : postdom
					.dominators(containingBlock)) {
				// strict postdomination
				if (!postdominatingBlock.equals(containingBlock)) {
					nodes.addAll(postdominatingBlock);
				}
			}

			// The containing block can also contain
			// dominators so we filter the containing block separately here and
			// add only the postdominating nodes.
			nodes.addAll(filterNodesAfterVariable(variable, containingBlock,
					!excludeCurrentFeature));

			// We don't normally want strict for required features so we add the
			// current node to the list, unless otherwise instructed
			if (!excludeCurrentFeature)
				nodes.add(variable);
		}

		if (includeFstr) {
			Domination dom = new Domination(graph);
			for (BasicBlock dominatingBlock : dom.dominators(containingBlock)) {
				// strict domination
				if (!dominatingBlock.equals(containingBlock)) {
					nodes.addAll(dominatingBlock);
				}
			}

			// The containing block can also contain postdominators so we filter
			// the containing block separately here and add only the dominating
			// nodes.
			nodes.addAll(filterNodesBeforeVariable(variable, containingBlock));

			// We want strict domination for FSTR so don't add the current node
		}

		return nodes;
	}

	private List<SimpleNode> filterNodesBeforeVariable(Name variableAtLocation,
			BasicBlock containingBlock) {
		final List<SimpleNode> nodesBeforeVariable = new ArrayList<SimpleNode>();

		for (SimpleNode node : containingBlock) {

			NodeFinder finder = new NodeFinder(variableAtLocation);
			try {
				node.accept(finder);
				if (finder.found) {
					break;
				} else {

					nodesBeforeVariable.add(node);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		return nodesBeforeVariable;
	}

	private List<SimpleNode> filterNodesAfterVariable(Name variableAtLocation,
			BasicBlock containingBlock, boolean includeNodeContainingVariable) {
		final List<SimpleNode> nodesAfterVariable = new ArrayList<SimpleNode>();

		boolean found = false;
		for (SimpleNode node : containingBlock) {

			if (!found) {
				NodeFinder finder = new NodeFinder(variableAtLocation);
				try {
					node.accept(finder);
					if (finder.found) {
						found = true;

						// In the special case that we are instructed to
						// include the variable's node, we do so here
						if (includeNodeContainingVariable) {
							nodesAfterVariable.add(node);
						}
					}

					// We continue in both cases as only the next node should
					// be added, not the node we found the variable in.
					continue;

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			nodesAfterVariable.add(node);
		}

		return nodesAfterVariable;
	}

	private class NodeFinder extends VisitorBase {

		private final SimpleNode node;
		boolean found = false;

		NodeFinder(SimpleNode node) {
			this.node = node;
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {

			if (node.equals(this.node)) {
				found = true;
			}
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}
	}
}
