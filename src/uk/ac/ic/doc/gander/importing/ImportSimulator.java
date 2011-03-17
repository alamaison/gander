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
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Importable;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Package;

public abstract class ImportSimulator {

	private Namespace importReceiver;

	public ImportSimulator(Namespace importReceiver) {
		this.importReceiver = importReceiver;
	}

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
			Package relativeToPackage, Namespace localNamespace)
			throws Exception {
		simulateImportHelper(importPath, relativeToPackage, localNamespace);
	}

	private void simulateImportAs(List<String> importPath,
			Package relativeToPackage, Namespace localNamespace, String as)
			throws Exception {

		Importable loaded = simulateImportHelper(importPath, relativeToPackage,
				null);

		importInto(localNamespace, loaded, as);
	}

	private Importable simulateImportHelper(List<String> importPath,
			Package relativeToPackage, Namespace importReceiver)
			throws Exception {

		List<String> processed = new LinkedList<String>();
		Importable loaded = null;
		for (String token : importPath) {
			processed.add(token);
			loaded = simulateLoad(processed, relativeToPackage);
			if (importReceiver != null)
				importInto(importReceiver, loaded, token);
			importReceiver = loaded;
		}

		return loaded;
	}

	private void simulateImportFromAs(List<String> fromPath, String itemName,
			Package relativeToPackage, Namespace localNamespace, String asName)
			throws Exception {

		Namespace namespaceToImportFrom = simulateImportHelper(fromPath,
				relativeToPackage, null);
		if (namespaceToImportFrom == null)
			return;

		List<String> itemPath = new ArrayList<String>(fromPath);
		itemPath.add(itemName);

		// Resolve item name to an item inside the namespace. It the item is a
		// module we have to load it, otherwise we can just investigate it
		Namespace importedNamespace = simulateLoad(itemPath, relativeToPackage);
		if (importedNamespace == null) {
			importedNamespace = namespaceToImportFrom.getClasses()
					.get(itemName);
			if (importedNamespace == null) {
				importedNamespace = namespaceToImportFrom.getFunctions().get(
						itemName);

				// TODO: The target of the 'from foo import bar' can
				// be a variable.
			}
		}

		if (importedNamespace != null) {
			importInto(importReceiver, importedNamespace, asName);
		}
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

	/**
	 * Try to load a module or package. As in Python, this attempts to load the
	 * importable relative to the given package, {@code relativeToPackage}, and
	 * if this fails attempts relative to the top-level package.
	 * 
	 * If neither of these succeeds it returns a special type to indicate import
	 * resolution failed.
	 * 
	 * @param importPath
	 *            Path of importable. Either relative or absolute.
	 * @param relativeToPackage
	 *            Package to begin relative importing in.
	 * @return {@link Module} or {@link Package} loaded into a {@link Type} if
	 *         loading succeeded, {@link TUnresolvedImport} otherwise.
	 */
	protected abstract Importable simulateLoad(List<String> importPath,
			Package relativeToPackage) throws Exception;

	protected abstract void importInto(Namespace scope, Namespace loaded,
			String as);
}