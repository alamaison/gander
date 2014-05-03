package uk.ac.ic.doc.gander.importing;

/**
 * Representation of an import statement at a particular location.
 * 
 * @param <C>
 *            type of object representing the code object in which the import
 *            appears
 * @param <M>
 *            type of object representing modules in the runtime model
 */
final class DefaultImport<C, M> implements Import<C, M> {

    /**
     * Creates new representation of the import statement.
     * 
     * @param statement
     *            representation of the import being simulated
     * @param relativeTo
     *            representation of the module that the import statement
     *            operates relative to; may be {@code null} as that could be a
     *            valid representation of the module object in some model of the
     *            system
     * @param container
     *            a representation of the code object whose code block contains
     *            the import statement
     */
    public static <C, M> DefaultImport<C, M> newImport(
            ImportStatement statement, M relativeTo, C container) {
        return new DefaultImport<C, M>(statement, relativeTo, container);
    }

    private final ImportStatement statement;
    private final M relativeTo;
    private final C container;

    @Override
    public ImportStatement statement() {
        return statement;
    }

    @Override
    public M relativeTo() {
        return relativeTo;
    }

    @Override
    public C container() {
        return container;
    }

    private DefaultImport(ImportStatement statement, M relativeTo, C container) {
        if (statement == null)
            throw new NullPointerException("Import statement not optional");
        if (container == null)
            throw new NullPointerException(
                    "Imports must have a container; the code object "
                            + "whose code block they appear in");
        if (container.equals(relativeTo))
            throw new IllegalArgumentException(
                    "An import is never relative to the module "
                            + "in which it appears");

        this.statement = statement;
        this.relativeTo = relativeTo;
        this.container = container;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((container == null) ? 0 : container.hashCode());
        result = prime * result
                + ((statement == null) ? 0 : statement.hashCode());
        result = prime * result
                + ((relativeTo == null) ? 0 : relativeTo.hashCode());
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
        DefaultImport<?, ?> other = (DefaultImport<?, ?>) obj;
        if (container == null) {
            if (other.container != null)
                return false;
        } else if (!container.equals(other.container))
            return false;
        if (statement == null) {
            if (other.statement != null)
                return false;
        } else if (!statement.equals(other.statement))
            return false;
        if (relativeTo == null) {
            if (other.relativeTo != null)
                return false;
        } else if (!relativeTo.equals(other.relativeTo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultImport [statement='" + statement + "', relativeTo="
                + relativeTo + ", container=" + container + "]";
    }

}
