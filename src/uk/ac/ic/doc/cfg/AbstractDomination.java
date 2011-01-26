package uk.ac.ic.doc.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.analysis.dominance.DomFront;
import uk.ac.ic.doc.analysis.dominance.DomMethod;
import uk.ac.ic.doc.analysis.dominance.Dominators;
import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

public abstract class AbstractDomination {

	protected Map<BasicBlock, DomFront.DomInfo> doms;

	protected AbstractDomination(Cfg graph, boolean postdom) {
		doms = new HashMap<BasicBlock, DomFront.DomInfo>();
		for (BasicBlock block : graph.getBlocks()) {
			doms.put(block, new DomFront.DomInfo());
		}
		Dominators.make(new DomMethod(graph), doms, postdom);
	}

	public Set<BasicBlock> getBlocks() {
		return doms.keySet();
	}

	public boolean dominates(BasicBlock dom, BasicBlock sub) {
		Collection<BasicBlock> doms = dominators(sub);
		if (doms == null)
			return false;

		return doms.contains(dom);
	}

	public Collection<BasicBlock> dominators(BasicBlock sub) {
		ArrayList<BasicBlock> dominators = new ArrayList<BasicBlock>();
		BasicBlock pos = sub;
		while (pos != null) {
			dominators.add(pos);
			BasicBlock idom = doms.get(pos).idom;
			if (pos == idom)
				break;
			pos = idom;
		}

		return dominators;
	}

}