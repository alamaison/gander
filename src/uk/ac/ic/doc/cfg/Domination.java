package uk.ac.ic.doc.cfg;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class Domination {

	private Set<BasicBlock> blocks = new HashSet<BasicBlock>();
	private Map<BasicBlock, Set<BasicBlock>> dominators =
		new HashMap<BasicBlock, Set<BasicBlock>>();

	public Domination(BasicBlock start) {
		addBlocks(start);

		for (BasicBlock block : blocks) {
			dominators.put(block, new HashSet<BasicBlock>(blocks));

			if (block == start) {
				Set<BasicBlock> startSet = new HashSet<BasicBlock>();
				startSet.add(start);
				dominators.put(block, startSet);
			} else
				dominators.put(block, new HashSet<BasicBlock>(blocks));
		}

		boolean changed = true;
		while (changed) {
			changed = false;

			for (BasicBlock block : blocks) {
				if (block == start)
					continue;

				Set<BasicBlock> updatedDoms = findPredecessorDoms(block);
				updatedDoms.add(block);

				if (!dominators.get(block).equals(updatedDoms)) {
					changed = true;
					dominators.put(block, updatedDoms);
				}
			}
		}
	}

	private static <T> Set<T> setIntersection(Set<T> set1, Set<T> set2) {
		Set<T> intersection = new HashSet<T>(set1);
		intersection.retainAll(set2);
		return intersection;
	}

	private Set<BasicBlock> findPredecessorDoms(BasicBlock block) {
		Iterator<BasicBlock> preds = block.getPredecessors().iterator();
		if (!preds.hasNext())
			return new HashSet<BasicBlock>();

		Set<BasicBlock> doms = new HashSet<BasicBlock>(
				dominators.get(preds.next()));
		
		while (preds.hasNext())
			doms = setIntersection(doms, dominators.get(preds.next()));

		return doms;
	}

	private void addBlocks(BasicBlock start) {
		if (blocks.contains(start))
			return;

		blocks.add(start);
		for (BasicBlock block : start.getOutSet())
			addBlocks(block);
	}

	public Set<BasicBlock> getBlocks() {
		return blocks;
	}

	public boolean dominates(BasicBlock dom, BasicBlock sub) {
		Collection<BasicBlock> doms = dominators.get(sub);
		if (doms == null)
			return false;

		return doms.contains(dom);
	}

}
