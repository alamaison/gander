package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Scope;

public class TPackage implements TImportable, ScopeType {

	private Package packageInstance;

	public TPackage(Package pkg) {
		assert pkg != null;
		this.packageInstance = pkg;
	}

	public Package getPackageInstance() {
		return packageInstance;
	}

	public Scope getScopeInstance() {
		return getPackageInstance();
	}
}
