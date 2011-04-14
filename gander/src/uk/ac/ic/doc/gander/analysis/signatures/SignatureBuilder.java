package uk.ac.ic.doc.gander.analysis.signatures;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.analysis.dominance.Domination;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;

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
	 */
	public Set<Call> signature(Name variable, BasicBlock containingBlock,
			Function enclosingFunction, Model model) {

		Set<BasicBlock> blocks = controlDependentBlocks(containingBlock,
				enclosingFunction.getCfg());

		Set<Call> calls = new PartialSignatureFromUsingVariable().buildSignature(
				variable, blocks, enclosingFunction);

		calls.addAll(new PartialSignatureFromPassingVariable().buildSignature(
				variable.id,
				blocks, enclosingFunction, model));
		return calls;
	}

	/**
	 * Return the blocks which are control-dependent on the given block.
	 */
	private static Set<BasicBlock> controlDependentBlocks(BasicBlock block,
			Cfg graph) {
		Set<BasicBlock> controlDependentBlocks = new HashSet<BasicBlock>();

		controlDependentBlocks.add(block);

		Domination dom = new Domination(graph);
		controlDependentBlocks.addAll(dom.dominators(block));

		Postdomination postdom = new Postdomination(graph);
		controlDependentBlocks.addAll(postdom.dominators(block));

		return controlDependentBlocks;
	}

}
