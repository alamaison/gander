package uk.ac.ic.doc.gander.analysis.ssa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;

public class GlobalsAndDefsFinder {

	private class GlobalAndDefsInspector extends NameInspector {
		private Set<String> varKill = new HashSet<String>();
		private BasicBlock block;

		public GlobalAndDefsInspector(BasicBlock block) throws Exception {
			this.block = block;
			inspect(block);
		}

		@Override
		public void seenLoad(Name name) {
			handleUse(name);
		}

		@Override
		public void seenStore(Name name) {
			handleDefinition(name);
		}

		@Override
		public void seenAugStore(Name name) {
			handleUse(name);
			handleDefinition(name);
		}

		private void handleUse(Name name) {
			if (!varKill.contains(name.id))
				globals.add(name.id);
		}

		private void handleDefinition(Name name) {
			addDefinitionToKillSet(name);
			addToDefinitions(name);
		}

		private void addDefinitionToKillSet(Name name) {
			varKill.add(name.id);
		}

		private void addToDefinitions(Name name) {
			Set<BasicBlock> blocksDefiningName = defLocations.get(name.id);
			if (blocksDefiningName == null) {
				blocksDefiningName = new HashSet<BasicBlock>();
				defLocations.put(name.id, blocksDefiningName);
			}
			blocksDefiningName.add(block);
		}
	}

	private Set<String> globals = new HashSet<String>();
	private Map<String, Set<BasicBlock>> defLocations = new HashMap<String, Set<BasicBlock>>();

	public GlobalsAndDefsFinder(Cfg graph) throws Exception {
		for (BasicBlock block : graph.getBlocks()) {
			new GlobalAndDefsInspector(block);
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
