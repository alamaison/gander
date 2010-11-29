package uk.ac.ic.doc.cfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class Postdomination extends AbstractDomination {

	public Postdomination(Set<BasicBlock> blocks, BasicBlock entry) {
		super(blocks, entry);
	}

	protected Set<BasicBlock> findPredecessorDoms(BasicBlock block) {
		Iterator<BasicBlock> preds = block.getSuccessors().iterator();
		if (!preds.hasNext())
			return new HashSet<BasicBlock>();

		Set<BasicBlock> doms = new HashSet<BasicBlock>(
				out.get(preds.next()));
		
		while (preds.hasNext())
			doms = setIntersection(doms, out.get(preds.next()));

		return doms;
	}

}
