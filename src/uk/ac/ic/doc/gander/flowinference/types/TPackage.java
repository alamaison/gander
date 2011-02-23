package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Scope;

public class TPackage implements ScopeType {

	private Package packageInstance;

	public TPackage(Package pkg) {
		this.packageInstance = pkg;
	}

	public Package getPackageInstance() {
		return packageInstance; 
	}

	public Scope getScopeInstance() {
		return getPackageInstance();
	}
}
