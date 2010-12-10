package uk.ac.ic.doc.cfg;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public abstract class AbstractDomination {

	protected Set<BasicBlock> blocks;

	protected Map<BasicBlock, Set<BasicBlock>> out = new HashMap<BasicBlock, Set<BasicBlock>>();

	public AbstractDomination(Set<BasicBlock> blocks, BasicBlock entry) {
		this.blocks = new HashSet<BasicBlock>(blocks);

		calculateDominators(entry);
	}

	protected static <T> Set<T> setIntersection(Set<T> set1, Set<T> set2) {
		Set<T> intersection = new HashSet<T>(set1);
		intersection.retainAll(set2);
		return intersection;
	}

	protected void calculateDominators(BasicBlock entry) {
		initialisation(entry);

		boolean changed = true;
		while (changed) {
			changed = false;

			for (BasicBlock block : blocks) {
				if (block == entry)
					continue;

				Set<BasicBlock> updatedDoms = findPredecessorDoms(block);
				updatedDoms.add(block);

				if (!out.get(block).equals(updatedDoms)) {
					changed = true;
					out.put(block, updatedDoms);
				}
			}
		}
	}

	private void initialisation(BasicBlock entry) {
		for (BasicBlock block : blocks) {
			out.put(block, new HashSet<BasicBlock>(blocks));

			if (block == entry) {
				Set<BasicBlock> startSet = new HashSet<BasicBlock>();
				startSet.add(entry);
				out.put(block, startSet);
			} else
				out.put(block, new HashSet<BasicBlock>(blocks));
		}
	}

	protected abstract Set<BasicBlock> findPredecessorDoms(BasicBlock block);

	public Set<BasicBlock> getBlocks() {
		return blocks;
	}

	public boolean dominates(BasicBlock dom, BasicBlock sub) {
		Collection<BasicBlock> doms = dominators(sub);
		if (doms == null)
			return false;

		return doms.contains(dom);
	}

	public Collection<BasicBlock> dominators(BasicBlock sub) {
		return out.get(sub);
	}

}