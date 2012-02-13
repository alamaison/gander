package uk.ac.ic.doc.gander.importing;

/**
 * Model of an import statement of the form {@code from x.y import *}.
 */
final class FromImportEverythingStatement implements ImportStatement {

	/**
	 * Creates representation of a starred, from-style import statement.
	 * 
	 * @param moduleImportPath
	 *            the path of the module with respect to which an item is being
	 *            imported; relative to code block in which the import statement
	 *            appeared (really relative to that code block's containing
	 *            module)
	 */
	static FromImportEverythingStatement newInstance(ImportPath moduleImportPath) {
		return new FromImportEverythingStatement(moduleImportPath);
	}

	private final ImportPath moduleImportPath;

	@Override
	public ImportPath boundObjectParentPath() {
		return moduleImportPath;
	}

	@Override
	public ImportPath modulePath() {
		return moduleImportPath;
	}

	@Override
	public boolean importsAreLimitedToModules() {
		return false;
	}

	@Override
	public BindingScheme bindingScheme() {

		return FromImportEverythingBindingScheme.INSTANCE;
	}

	private FromImportEverythingStatement(ImportPath moduleImportPath) {
		if (moduleImportPath == null)
			throw new NullPointerException("Module path is not optional");
		if (moduleImportPath.isEmpty())
			throw new IllegalArgumentException("Module path cannot be empty");

		this.moduleImportPath = moduleImportPath;
	}

	@Override
	public String toString() {
		return "from " + moduleImportPath + " import *";
	}

}
