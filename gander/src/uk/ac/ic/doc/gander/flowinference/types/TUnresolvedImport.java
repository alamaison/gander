package uk.ac.ic.doc.gander.flowinference.types;

import java.util.List;

import uk.ac.ic.doc.gander.flowinference.UnresolvedImportError;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Namespace;

public class TUnresolvedImport implements TImportable, TNamespace {

	private List<String> importPath;
	private Package relativeToPackage;
	private String fromTarget;

	public TUnresolvedImport(List<String> importPath, Package relativeToPackage) {
		this.importPath = importPath;
		this.relativeToPackage = relativeToPackage;
	}

	public TUnresolvedImport(List<String> fromPath, String itemName,
			Package relativeToPackage) {
		this.importPath = fromPath;
		this.relativeToPackage = relativeToPackage;
		this.fromTarget = itemName;
	}

	public Namespace getNamespaceInstance() {
		throw new UnresolvedImportError(importPath, relativeToPackage);
	}

	public List<String> getImportPath() {
		return importPath;
	}

	public Package getRelativeToPackage() {
		return relativeToPackage;
	}
	
	public String getFromTarget() {
		return fromTarget;
	}
}
