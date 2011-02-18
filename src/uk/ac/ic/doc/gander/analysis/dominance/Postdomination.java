package uk.ac.ic.doc.gander.analysis.dominance;

import uk.ac.ic.doc.gander.cfg.model.Cfg;

public class Postdomination extends AbstractDomination {

	public Postdomination(Cfg graph) {
		super(graph, true);
	}

}
