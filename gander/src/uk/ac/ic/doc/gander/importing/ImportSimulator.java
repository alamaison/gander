package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.model.Module;

/**
 * Simulates the Python import mechanism.
 * 
 * This includes such complications as importing parent packages whenever a
 * child module or package is imported. Actually loading modules and packages,
 * and binding them to names isn't handled by this class. Instead it is left to
 * subclasses.
 * 
 * Subclasses must decide how to react to two different aspects of the Python
 * import mechanism. First, modules and packages are loaded. Subclasses are
 * given a path relative to a previously loaded package but they are free to
 * implement the loading operation however they choose. All that is required is
 * that they return a {@link Module} if the load succeeded or null if it fails.
 * The second aspect is name binding. The whole point of importing is to bind a
 * name to a loaded module or other namespace. Subclasses are free to interpret
 * name binding however makes sense for their task or even ignore it completely.
 */
public interface ImportSimulator {

	public void simulateImport(String moduleName);

	public void simulateImportAs(String moduleName, String asName);

	public void simulateImportFrom(String moduleName, String itemName);

	public void simulateImportFromAs(String moduleName, String itemName,
			String asName);

}