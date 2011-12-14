package uk.ac.ic.doc.gander.flowinference.types;

import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TUnresolvedImport implements TCodeObject {

	private List<String> importPath;
	private Module relativeToPackage;
	private String fromTarget;

	public TUnresolvedImport(List<String> importPath, Module relativeToPackage) {
		assert importPath != null;
		// relativeToPackage may be null when import occurs in top level e.g.
		// builtins

		this.importPath = importPath;
		this.relativeToPackage = relativeToPackage;
	}

	public TUnresolvedImport(List<String> fromPath, String itemName,
			Module relativeToPackage) {
		this.importPath = fromPath;
		this.relativeToPackage = relativeToPackage;
		this.fromTarget = itemName;
	}

	public CodeObject codeObject() {
		return null;
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

	/**
	 * {@inheritDoc}
	 * 
	 * Members on an unresolved import cannot be accurately typed so we
	 * approximate it conservatively as Top.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromTarget == null) ? 0 : fromTarget.hashCode());
		result = prime * result
				+ ((importPath == null) ? 0 : importPath.hashCode());
		result = prime
				* result
				+ ((relativeToPackage == null) ? 0 : relativeToPackage
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TUnresolvedImport other = (TUnresolvedImport) obj;
		if (fromTarget == null) {
			if (other.fromTarget != null)
				return false;
		} else if (!fromTarget.equals(other.fromTarget))
			return false;
		if (importPath == null) {
			if (other.importPath != null)
				return false;
		} else if (!importPath.equals(other.importPath))
			return false;
		if (relativeToPackage == null) {
			if (other.relativeToPackage != null)
				return false;
		} else if (!relativeToPackage.equals(other.relativeToPackage))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TUnresolvedImport [fromTarget=" + fromTarget + ", importPath="
				+ importPath + ", relativeToPackage=" + relativeToPackage + "]";
	}

}
