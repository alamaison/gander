/**
 * 
 */
package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TPackage;
import uk.ac.ic.doc.gander.model.Importable;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Package;

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
 * that they return an {@link Importable} if the load succeeded or null if it
 * fails. The second aspect is name binding. The whole point of importing is to
 * bind a name to a loaded module or other namespace. Subclasses are free to
 * interpret name binding however makes sense for their task or even ignore it
 * completely.
 */
public abstract class ImportSimulator {

	private Namespace importReceiver;
	private Package topLevel;

	public ImportSimulator(Namespace importReceiver, Package topLevel) {
		this.importReceiver = importReceiver;
		this.topLevel = topLevel;
	}

	/**
	 * Load a module or package.
	 * 
	 * If loading fails, return {@code null}.
	 * 
	 * @param importPath
	 *            Path of importable. Relative to the given package.
	 * @param relativeToPackage
	 *            Package to begin relative importing in.
	 * @return {@link Module} or {@link Package} if loading succeeded, {@code
	 *         null} otherwise.
	 */
	protected abstract Importable simulateLoad(List<String> importPath,
			Package relativeToPackage) throws Exception;

	protected abstract void bindName(Namespace importReceiver,
			Namespace loaded, String as);

	protected abstract void onUnresolvedImportFrom(List<String> fromPath,
			String itemName, Package relativeToPackage,
			Namespace importReceiver, String as);

	protected abstract void onUnresolvedImport(List<String> importPath,
			Package relativeToPackage, Namespace importReceiver, String as);

	public void simulateImportFrom(String fromName, String itemName)
			throws Exception {
		simulateImportFromAs(fromName, itemName, itemName);
	}

	public void simulateImportFromAs(String fromName, String itemName,
			String asName) throws Exception {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(fromName));

		Package parentPackage = findParentPackage(importReceiver);

		simulateImportFromAs(tokens, itemName, parentPackage, importReceiver,
				asName);
	}

	public void simulateImportAs(String importName, String asName)
			throws Exception {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		Package parentPackage = findParentPackage(importReceiver);

		simulateImportAs(tokens, parentPackage, importReceiver, asName);
	}

	public void simulateImport(String importName) throws Exception {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		Package parentPackage = findParentPackage(importReceiver);
		simulateImport(tokens, parentPackage, importReceiver);
	}

	/**
	 * Import a module as in {@code import foo.bar.baz}.
	 * 
	 * Binds the importable namespace named by the first token in the dotted
	 * import path to that name in the local namespace (be that a package,
	 * module, class or function) and binds the modules named by any subsequent
	 * tokens to that name in the previously bound importable namespace.
	 * 
	 * In other words, when importing {@code x.y.z}, Python will import {@code
	 * z} into {@code y} and {@code y} into {@code x}. We simulate that as well
	 * by adding a symbol {@code z} to {@code y}'s symbol table pointing to the
	 * loaded {@link TModule} or {@link TPackage} {@code z} and likewise for
	 * {@code y} in {@code x}.
	 */
	private void simulateImport(List<String> importPath,
			Package relativeToPackage, Namespace importReceiver)
			throws Exception {
		simulateImportHelper(importPath, relativeToPackage, importReceiver);
	}

	private void simulateImportAs(List<String> importPath,
			Package relativeToPackage, Namespace importReceiver, String as)
			throws Exception {

		Importable loaded = simulateImportHelper(importPath, relativeToPackage,
				null);
		handleBind(importPath, relativeToPackage, importReceiver, as, loaded);
	}

	private void simulateImportFromAs(List<String> fromPath, String itemName,
			Package relativeToPackage, Namespace importReceiver, String asName)
			throws Exception {

		Namespace namespaceToImportFrom = simulateImportHelper(fromPath,
				relativeToPackage, null);
		if (namespaceToImportFrom == null)
			return;

		List<String> itemPath = new ArrayList<String>(fromPath);
		itemPath.add(itemName);

		// Resolve item name to an item inside the namespace. It the item is a
		// module we have to load it, otherwise we can just investigate it
		Namespace loaded = simulateTwoStepLoad(itemPath, relativeToPackage);
		if (loaded == null) {
			loaded = namespaceToImportFrom.getClasses().get(itemName);
			if (loaded == null) {
				loaded = namespaceToImportFrom.getFunctions().get(itemName);

				// TODO: The target of the 'from foo import bar' can
				// be a variable.
			}
		}

		handleBindFrom(fromPath, itemName, relativeToPackage, importReceiver,
				asName, loaded);
	}

	private Importable simulateImportHelper(List<String> importPath,
			Package relativeToPackage, Namespace importReceiver)
			throws Exception {

		List<String> processed = new LinkedList<String>();
		Importable loaded = null;
		for (String token : importPath) {
			processed.add(token);
			loaded = simulateTwoStepLoad(processed, relativeToPackage);
			if (loaded == null) {
				onUnresolvedImport(importPath, relativeToPackage,
						importReceiver, token);
				break; // abort import
			} else {
				if (importReceiver != null)
					bindName(importReceiver, loaded, token);
				importReceiver = loaded;
			}
		}

		return loaded;
	}

	/**
	 * Try to load a module or package. As in Python, this attempts to load the
	 * importable relative to the given package, {@code relativeToPackage}, and
	 * if this fails attempts relative to the top-level package.
	 * 
	 * If neither of these succeeds it returns null to indicate import
	 * resolution failed.
	 * 
	 * @param importPath
	 *            Path of importable. Either relative or absolute.
	 * @param relativeToPackage
	 *            Package to begin relative importing in.
	 * @return {@link Module} or {@link Package} if loading succeeded, {@code
	 *         null} otherwise.
	 */
	private Importable simulateTwoStepLoad(List<String> importPath,
			Package relativeToPackage) throws Exception {
		Importable loaded = null;

		if (relativeToPackage != null)
			loaded = simulateLoad(importPath, relativeToPackage);

		if (loaded == null)
			loaded = simulateLoad(importPath, topLevel);

		return loaded;
	}

	private void handleBind(List<String> importPath, Package relativeToPackage,
			Namespace importReceiver, String as, Importable loaded) {
		if (loaded != null)
			bindName(importReceiver, loaded, as);
		else
			onUnresolvedImport(importPath, relativeToPackage, importReceiver,
					as);
	}

	private void handleBindFrom(List<String> fromPath, String itemName,
			Package relativeToPackage, Namespace importReceiver, String as,
			Namespace loaded) {
		if (loaded != null)
			bindName(importReceiver, loaded, as);
		else
			onUnresolvedImportFrom(fromPath, itemName, relativeToPackage,
					importReceiver, as);
	}

	private Package findParentPackage(Namespace scope) {
		Namespace parent = scope.getParentScope();
		if (parent == null)
			return null;

		if (parent instanceof Package)
			return (Package) parent;
		else
			return findParentPackage(parent.getParentScope());
	}
}