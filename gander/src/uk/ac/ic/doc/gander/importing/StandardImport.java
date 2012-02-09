package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

/**
 * Representation of an {@code import module} statement at a particular
 * location.
 * 
 * @param <O>
 *            the type of Java objects representing general Python objects that
 *            can be imported (including modules and other code objects)
 * @param <C>
 *            type of object representing the code object in which the import
 *            appears
 * @param <M>
 *            type of object representing modules in the runtime model
 */
final class StandardImport<O, C, M> implements Import<O, C, M> {

	private final StaticImportSpecification specification;
	private final M relativeTo;
	private final C container;

	/**
	 * Creates new representation of the import statement.
	 * 
	 * @param specification
	 *            specification of the import being simulated
	 * @param relativeTo
	 *            representation of the module that the import statement
	 *            operates relative to; may be {@code null} as that could be a
	 *            valid representation of the module object in some model of the
	 *            system
	 * @param container
	 *            a representation of the code object whose code block contains
	 *            the import statement
	 */
	StandardImport(StandardImportSpecification specification, M relativeTo,
			C container) {
		if (specification == null)
			throw new NullPointerException("Import specification not optional");
		if (container == null)
			throw new NullPointerException(
					"Imports must have a container; the code object "
							+ "whose code block they appear in");
		if (container.equals(relativeTo))
			throw new IllegalArgumentException(
					"An import is never relative to the module "
							+ "in which it appears");

		this.specification = specification;
		this.relativeTo = relativeTo;
		this.container = container;
	}

	public StaticImportSpecification specification() {
		return specification;
	}

	public M relativeTo() {
		return relativeTo;
	}

	public C container() {
		return container;
	}

	public BindingScheme<M> newBindingScheme(Binder<O, C, M> bindingHandler,
			Loader<O, M> loader) {
		return new StandardImportBindingScheme<O, C, M>(this, bindingHandler);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((container == null) ? 0 : container.hashCode());
		result = prime * result
				+ ((specification == null) ? 0 : specification.hashCode());
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
		StandardImport<?, ?, ?> other = (StandardImport<?, ?, ?>) obj;
		if (container == null) {
			if (other.container != null)
				return false;
		} else if (!container.equals(other.container))
			return false;
		if (specification == null) {
			if (other.specification != null)
				return false;
		} else if (!specification.equals(other.specification))
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
		return "StandardImport [specification='" + specification
				+ "', relativeTo=" + relativeTo + ", container=" + container
				+ "]";
	}

}
