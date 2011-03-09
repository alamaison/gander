package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.python.pydev.parser.jython.ParseException;

public abstract class Package implements Importable {

	private String name;
	private Package parent;

	public Package(String name, Package parent) throws IOException,
			ParseException, InvalidElementException {
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
}
