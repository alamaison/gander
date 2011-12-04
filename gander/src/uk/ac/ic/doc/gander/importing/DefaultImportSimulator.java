package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

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
public final class DefaultImportSimulator implements ImportSimulator {

	public interface ImportEvents {

		/**
		 * Load a module or package relative to the given package.
		 * 
		 * If loading fails, return {@code null}.
		 * 
		 * @param importPath
		 *            Path of importable. Relative to the given package.
		 * @param relativeToPackage
		 *            SourceFile to begin relative importing in.
		 * @return {@link Module} if loading succeeded, {@code null} otherwise.
		 */
		Module simulateLoad(List<String> importPath, Module relativeToPackage);

		/**
		 * Load a module or package relative to the top level.
		 * 
		 * If loading fails, return {@code null}.
		 * 
		 * @param importPath
		 *            Absolute path of importable.
		 * @return {@link Module} if loading succeeded, {@code null} otherwise.
		 */
		Module simulateLoad(List<String> importPath);

		void bindName(Namespace importReceiver, Namespace loaded, String as);

		void onUnresolvedImport(List<String> importPath,
				Module relativeToPackage, Namespace importReceiver, String as);

		void onUnresolvedImportFromItem(List<String> fromPath, String itemName,
				Module relativeToPackage, Namespace importReceiver, String as);
	}

	private final Namespace importReceiver;
	private final ImportEvents eventHandler;

	/**
	 * FIXME: importReceiver may not actually be the import receiver. It depends
	 * on the binding scope of 'as' in importReceiver. It could be the global
	 * scope.
	 */
	public DefaultImportSimulator(Namespace importReceiver, ImportEvents eventHandler) {
		if (importReceiver == null)
			throw new NullPointerException("Must have namespace to import into");
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");

		this.importReceiver = importReceiver;
		this.eventHandler = eventHandler;
	}

	public void simulateImportFrom(String fromName, String itemName) {
		simulateImportFromAs(fromName, itemName, itemName);
	}

	public void simulateImportFromAs(String fromName, String itemName,
			String asName) {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(fromName));

		Module parentPackage = findParentPackage(importReceiver);

		simulateImportFromAs(tokens, itemName, parentPackage, importReceiver,
				asName);
	}

	public void simulateImportAs(String importName, String asName) {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		Module parentPackage = findParentPackage(importReceiver);

		simulateImportAs(tokens, parentPackage, importReceiver, asName);
	}

	public void simulateImport(String importName) {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		Module parentPackage = findParentPackage(importReceiver);
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
			Module relativeToPackage, Namespace importReceiver) {
		simulateImportHelper(importPath, relativeToPackage, importReceiver);
	}

	private void simulateImportAs(List<String> importPath,
			Module relativeToPackage, Namespace importReceiver, String as) {

		Module loaded = simulateImportHelper(importPath, relativeToPackage,
				null);
		handleBind(importPath, relativeToPackage, importReceiver, as, loaded);
	}

	private void simulateImportFromAs(List<String> fromPath, String itemName,
			Module relativeToPackage, Namespace importReceiver, String asName) {

		Namespace namespaceToImportFrom = simulateImportHelper(fromPath,
				relativeToPackage, null);
		if (namespaceToImportFrom == null)
			// Reporting this failure is handled by simulateImportHelper
			return;

		List<String> itemPath = new ArrayList<String>(fromPath);
		itemPath.add(itemName);

		// Resolve item name to an item inside the namespace. If the item is a
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

	private Module simulateImportHelper(List<String> importPath,
			Module relativeToPackage, Namespace importReceiver) {

		List<String> processed = new LinkedList<String>();
		Module loaded = null;
		for (String token : importPath) {
			processed.add(token);
			loaded = simulateTwoStepLoad(processed, relativeToPackage);
			if (loaded == null) {
				eventHandler.onUnresolvedImport(importPath, relativeToPackage,
						importReceiver, token);
				break; // abort import
			} else {
				if (importReceiver != null)
					eventHandler.bindName(importReceiver, loaded, token);
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
	 *            SourceFile to begin relative importing in.
	 * @return {@link Module} if loading succeeded, {@code null} otherwise.
	 */
	private Module simulateTwoStepLoad(List<String> importPath,
			Module relativeToPackage) {
		Module loaded = null;

		if (relativeToPackage != null)
			loaded = eventHandler.simulateLoad(importPath, relativeToPackage);

		if (loaded == null)
			loaded = eventHandler.simulateLoad(importPath);

		return loaded;
	}

	private void handleBind(List<String> importPath, Module relativeToPackage,
			Namespace importReceiver, String as, Module loaded) {
		if (loaded != null)
			eventHandler.bindName(importReceiver, loaded, as);
		else
			eventHandler.onUnresolvedImport(importPath, relativeToPackage,
					importReceiver, as);
	}

	private void handleBindFrom(List<String> fromPath, String itemName,
			Module relativeToPackage, Namespace importReceiver, String as,
			Namespace loaded) {
		if (loaded != null)
			eventHandler.bindName(importReceiver, loaded, as);
		else
			eventHandler.onUnresolvedImportFromItem(fromPath, itemName,
					relativeToPackage, importReceiver, as);
	}

	private Module findParentPackage(Namespace scope) {
		Namespace parent = scope.getParentScope();
		if (parent == null)
			return null;

		if (parent instanceof Module)
			return (Module) parent;
		else
			return findParentPackage(parent.getParentScope());
	}
}