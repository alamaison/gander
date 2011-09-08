package uk.ac.ic.doc.gander.flowinference.types;

import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.UnresolvedImportError;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

public class TUnresolvedImport implements TImportable, TNamespace {

	private List<String> importPath;
	private Module relativeToPackage;
	private String fromTarget;

	public TUnresolvedImport(List<String> importPath, Module relativeToPackage) {
		this.importPath = importPath;
		this.relativeToPackage = relativeToPackage;
	}

	public TUnresolvedImport(List<String> fromPath, String itemName,
			Module relativeToPackage) {
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

	public Module getRelativeToPackage() {
		return relativeToPackage;
	}

	public String getFromTarget() {
		return fromTarget;
	}

	public String getName() {
		// TODO: use relative-to path in the import info somehow
		return "<unresolved import: '" + reconstructImportSpec() + "'>";
	}

	private String reconstructImportSpec() {
		if (fromTarget.isEmpty()) {
			return "import " + DottedName.toDottedName(importPath);
		} else {
			return "from " + DottedName.toDottedName(importPath) + " import "
					+ fromTarget;
		}
	}
}
