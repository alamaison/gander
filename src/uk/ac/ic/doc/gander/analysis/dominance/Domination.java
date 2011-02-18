package uk.ac.ic.doc.gander.analysis.dominance;

import java.util.Map;

import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;

public class Domination extends AbstractDomination {

	public Domination(Cfg graph) {
		super(graph, false);
	}
	
	public Domination(Map<BasicBlock, DomFront.DomInfo> doms) {
		super(doms);
	}

}
