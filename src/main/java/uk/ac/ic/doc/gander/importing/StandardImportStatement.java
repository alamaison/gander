package uk.ac.ic.doc.gander.importing;

/**
 * Model of an import statement of the form {@code import x.y.z}.
 */
final class StandardImportStatement implements StaticImportStatement {

    private final ImportPath moduleImportPath;

    /**
     * Creates representation of a standard (non-from) import statement.
     * 
     * @param moduleImportPath
     *            the path of the module being imported relative to the code
     *            block in which it appeared (really relative to that code
     *            block's containing module)
     */
    static StandardImportStatement newInstance(ImportPath moduleImportPath) {
        return new StandardImportStatement(moduleImportPath);
    }

    @Override
    public String bindingName() {
        return moduleImportPath.get(0);
    }

    @Override
    public String boundObjectName() {
        return moduleImportPath.get(0);
    }

    @Override
    public ImportPath modulePath() {
        return moduleImportPath;
    }

    @Override
    public ImportPath boundObjectParentPath() {
        return ImportPath.EMPTY_PATH;
    }

    @Override
    public boolean importsAreLimitedToModules() {
        return true;
    }

    @Override
    public BindingScheme bindingScheme() {

        return StandardImportBindingScheme.INSTANCE;
    }

    private StandardImportStatement(ImportPath moduleImportPath) {
        if (moduleImportPath == null)
            throw new NullPointerException("Module path is not optional");
        if (moduleImportPath.isEmpty())
            throw new IllegalArgumentException("Module path cannot be empty");

        this.moduleImportPath = moduleImportPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((moduleImportPath == null) ? 0 : moduleImportPath.hashCode());
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
        StandardImportStatement other = (StandardImportStatement) obj;
        if (moduleImportPath == null) {
            if (other.moduleImportPath != null)
                return false;
        } else if (!moduleImportPath.equals(other.moduleImportPath))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "import " + moduleImportPath;
    }

}
