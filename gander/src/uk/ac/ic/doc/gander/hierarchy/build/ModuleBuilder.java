package uk.ac.ic.doc.gander.hierarchy.build;

import java.io.File;

import uk.ac.ic.doc.gander.hierarchy.InvalidElementException;
import uk.ac.ic.doc.gander.hierarchy.Module;
import uk.ac.ic.doc.gander.hierarchy.Package;

class ModuleBuilder extends Builder {

	private Module module;

	ModuleBuilder(File moduleFile, Package parent, boolean isSystem)
			throws InvalidElementException {
		String name = moduleNameFromFile(moduleFile);
		if (name == null)
			throw new InvalidElementException("Not a module", moduleFile);

		this.module = new Module(name, moduleFile, parent, isSystem);
	}

	Module getModule() {
		return module;
	}

}
