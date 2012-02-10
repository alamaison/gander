package uk.ac.ic.doc.gander.importing;

/**
 * Callback for simulations of Python import mechanism.
 */
public interface ImportHandler<T> {

	/**
	 * Seen an import statement.
	 * 
	 * Compound imports such as {@code import x.y.z, a.b, foo as bar} are broken
	 * up into multiple calls to this method with a separate
	 * {@link StaticImportSpecification} for each.
	 * 
	 * @param importReceiver
	 *            code object containing this import statement
	 * @param importStatement
	 *            object representing the import
	 */
	void onImport(T importReceiver, StaticImportSpecification importStatement);

}