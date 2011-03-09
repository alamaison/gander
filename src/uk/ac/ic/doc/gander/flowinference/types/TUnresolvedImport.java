package uk.ac.ic.doc.gander.flowinference.types;

import java.util.List;

import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Scope;

public class TUnresolvedImport implements TImportable, ScopeType {

	private List<String> importPath;
	private Package relativeToPackage;

	public TUnresolvedImport(List<String> importPath, Package relativeToPackage) {
		this.importPath = importPath;
		this.relativeToPackage = relativeToPackage;
	}

	public Scope getScopeInstance() {
		return null;
	}

	public List<String> getImportPath() {
		return importPath;
	}

	public Package getRelativeToPackage() {
		return relativeToPackage;
	}
}
