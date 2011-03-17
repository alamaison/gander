package uk.ac.ic.doc.gander.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class Package implements Importable {

	private String name;
	private Package parent;

	public Package(String name, Package parent) {
		this.name = name;
		this.parent = parent;
	}

	public boolean isTopLevel() {
		return getParentPackage() == null;
	}
	
	public String getName() {
		return name;
	}

	public String getFullName() {
		if (isTopLevel())
			return getName();
		else {
			String parentName = parent.getFullName();
			if ("".equals(parentName))
				return getName();
			else
				return parentName + "." + getName();
		}
	}

	public Module lookupModule(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Package scope = this;
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getModules().get(token);
			else
				scope = scope.getPackages().get(token);
		}

		return null;
	}

	public Package lookupPackage(List<String> importNameTokens) {
		Queue<String> tokens = new LinkedList<String>(importNameTokens);

		Package scope = this;
		while (scope != null && !tokens.isEmpty()) {
			String token = tokens.remove();
			if (tokens.isEmpty())
				return scope.getPackages().get(token);
			else
				scope = scope.getPackages().get(token);
		}

		return scope;
	}

	public Package getParentPackage() {
		return parent;
	}

	public Namespace getParentScope() {
		return getParentPackage();
	}

	@Override
	public String toString() {
		return "Package["+getFullName()+"]";
	}
	
}
