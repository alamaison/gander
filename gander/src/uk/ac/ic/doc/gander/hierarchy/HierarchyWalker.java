package uk.ac.ic.doc.gander.hierarchy;

import java.util.Map.Entry;

public abstract class HierarchyWalker {

	public final void walk(Hierarchy hierarchy) throws Exception {
		visitPackage(hierarchy.getTopLevelPackage());
		walkThroughPackage(hierarchy.getTopLevelPackage());
	}

	private void walkThroughPackage(Package pkg) throws Exception {
		for (Entry<String, Package> subpkg : pkg.getPackages().entrySet()) {
			visitPackage(subpkg.getValue());
			walkThroughPackage(subpkg.getValue());
		}
		for (Entry<String, Module> module : pkg.getModules().entrySet()) {
			visitModule(module.getValue());
		}
	}

	protected void visitPackage(Package pkg) throws Exception {
	}

	protected void visitModule(Module value) throws Exception {
	}
}
