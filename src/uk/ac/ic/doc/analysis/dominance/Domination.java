package uk.ac.ic.doc.analysis.dominance;

import java.util.Map;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

public class Domination extends AbstractDomination {

	public Domination(Cfg graph) {
		super(graph, false);
	}
	
	public Domination(Map<BasicBlock, DomFront.DomInfo> doms) {
		super(doms);
	}

}
