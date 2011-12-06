package uk.ac.ic.doc.gander.importing;

/**
 * Callback for simulations of Python import mechanism.
 */
public interface ImportHandler<T> {

	/**
	 * Seen {@code import x.y.z} style import.
	 * 
	 * Compound imports such as {@code import x.y.z, a.b, foo as bar} are broken
	 * up into multiple calls to this method or {@link onImportAs}.
	 * 
	 * @param importReceiver
	 *            code object containing this import statement
	 * @param moduleName
	 *            name of module relative to code block in which it appeared
	 *            (really relative to that code block's containing module)
	 */
	void onImport(T importReceiver, String moduleName);

	/**
	 * 
	 * Seen {@code import x.y.z as bar} style import.
	 * 
	 * Compound imports such as {@code import x.y.z as bar, a.b, foo} are broken
	 * up into multiple calls to this method or {@link onImport}.
	 * 
	 * @param importReceiver
	 *            code object containing this import statement
	 * @param moduleName
	 *            name of module relative to code block in which it appeared
	 *            (really relative to that code block's containing module)
	 * @param asName
	 *            name to which the imported module's code object is bound in
	 *            the namespace of the code block in which this import statement
	 *            appeared
	 */
	void onImportAs(T importReceiver, String moduleName, String asName);

	/**
	 * Seen {@code from x.y import i} style import.
	 * 
	 * Compound imports such as {@code from x.y import i, j, k as p} are broken
	 * up into multiple calls to this method or {@link onImportFromAs}.
	 * 
	 * @param importReceiver
	 *            code object containing this import statement
	 * @param moduleName
	 *            name of module relative to code block in which it appeared
	 *            (really relative to that code block's containing module)
	 * @param itemName
	 *            name of object in moduleName's namespace being imported into
	 *            the namespace of the code block in which this import statement
	 *            appeared
	 */
	void onImportFrom(T importReceiver, String moduleName,
			String itemName);

	/**
	 * Seen {@code from x.y import i as p} style import.
	 * 
	 * Compound imports such as {@code from x.y import i as p, j, k as q} are
	 * broken up into multiple calls to this method or {@link onImportFrom}.
	 * 
	 * @param importReceiver
	 *            code object containing this import statement
	 * @param moduleName
	 *            name of module relative to code block in which it appeared
	 *            (really relative to that code block's containing module)
	 * @param itemName
	 *            name of object in moduleName's namespace being imported into
	 *            the namespace of the code block in which this import statement
	 *            appeared
	 * @param asName
	 *            name to which the imported object is bound in the namespace of
	 *            the code block in which this import statement appeared
	 */
	void onImportFromAs(T importReceiver, String moduleName,
			String itemName, String asName);

}