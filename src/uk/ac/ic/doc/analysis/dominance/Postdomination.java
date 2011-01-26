package uk.ac.ic.doc.analysis.dominance;

import uk.ac.ic.doc.cfg.model.Cfg;

public class Postdomination extends AbstractDomination {

	public Postdomination(Cfg graph) {
		super(graph, true);
	}

}
