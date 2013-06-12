package uk.ac.ic.doc.gander.importing;

/**
 * Callback for simulations of Python import mechanism.
 */
public interface ImportHandler<C, M> {

	/**
	 * Seen an import statement.
	 * 
	 * Compound imports such as {@code import x.y.z, a.b, foo as bar} are broken
	 * up into multiple calls to this method with a separate {@link Import} for
	 * each.
	 * 
	 * @param importInstance
	 *            object representing the import and the location it appears
	 */
	void onImport(Import<C, M> importInstance);

}