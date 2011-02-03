package uk.ac.ic.doc.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;

public class GlobalsAndDefsFinder {

	private Set<String> globals = new HashSet<String>();
	private Map<String, Set<BasicBlock>> defLocations = new HashMap<String, Set<BasicBlock>>();

	public GlobalsAndDefsFinder(Cfg graph) throws Exception {
		for (BasicBlock block : graph.getBlocks()) {
			HashSet<String> varKill = new HashSet<String>();
			DefUseSeparator defUses = new DefUseSeparator(block);
			for (IDefUse op : defUses.operations()) {
				String name = op.getName().id;
				if (op instanceof Use) {
					if (!varKill.contains(name))
						globals.add(name);
				} else {
					assert op instanceof Def;
					varKill.add(name);

					Set<BasicBlock> blocksDefiningName = defLocations.get(name);
					if (blocksDefiningName == null) {
						blocksDefiningName = new HashSet<BasicBlock>();
						defLocations.put(name, blocksDefiningName);
					}
					blocksDefiningName.add(block);
				}
			}
		}
	}

	public Set<String> globals() {
		return globals;
	}

	public Iterable<String> definitions() {
		return defLocations.keySet();
	}

	public Collection<BasicBlock> definingLocations(String name) {
		return defLocations.get(name);
	}

}
