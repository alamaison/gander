/**
 * 
 */
package uk.ac.ic.doc.gander.flowinference;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TImportable;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TPackage;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Namespace;

abstract class ImportSimulator {

	private Namespace importReceiver;
	private Package topLevel;

	ImportSimulator(Namespace importReceiver, Package topLevel) {
		this.importReceiver = importReceiver;
		this.topLevel = topLevel;
	}

	void simulateImportFrom(String fromName, String itemName) {
		simulateImportFromAs(fromName, itemName, itemName);
	}

	void simulateImportFromAs(String fromName, String itemName, String asName) {
		List<String> tokens = new LinkedList<String>(SymbolTable
				.dottedNameToImportTokens(fromName));

		Package parentPackage = findParentPackage(importReceiver);

		simulateImportFromAs(tokens, itemName, parentPackage, importReceiver,
				asName);
	}

	void simulateImportAs(String importName, String asName) {
		List<String> tokens = new LinkedList<String>(SymbolTable
				.dottedNameToImportTokens(importName));

		Package parentPackage = findParentPackage(importReceiver);

		simulateImportAs(tokens, parentPackage, importReceiver, asName);
	}

	void simulateImport(String importName) {
		List<String> tokens = new LinkedList<String>(SymbolTable
				.dottedNameToImportTokens(importName));

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
			Package relativeToPackage, Namespace localNamespace) {
		simulateImportHelper(importPath, relativeToPackage, localNamespace);
	}

	private void simulateImportAs(List<String> importPath,
			Package relativeToPackage, Namespace localNamespace, String as) {

		TImportable loaded = simulateImportHelper(importPath,
				relativeToPackage, null);

		importInto(localNamespace, loaded, as);
	}

	private TImportable simulateImportHelper(List<String> importPath,
			Package relativeToPackage, Namespace importReceiver) {

		List<String> processed = new LinkedList<String>();
		TImportable loaded = null;
		for (String token : importPath) {
			processed.add(token);
			loaded = simulateLoad(processed, relativeToPackage);
			if (importReceiver != null)
				importInto(importReceiver, loaded, token);
			importReceiver = loaded.getNamespaceInstance();
		}

		return loaded;
	}

	void simulateImportFromAs(List<String> fromPath, String itemName,
			Package relativeToPackage, Namespace localNamespace, String asName) {

		Namespace namespaceToImportFrom = simulateImportHelper(fromPath,
				relativeToPackage, null).getNamespaceInstance();

		// Resolve item name to an item inside the namespace
		Type type = null;
		Package pkg = namespaceToImportFrom.getPackages().get(itemName);
		if (pkg != null) {
			type = new TPackage(pkg);
		} else {
			Module submodule = namespaceToImportFrom.getModules().get(itemName);
			if (submodule != null) {
				type = new TModule(submodule);
			} else {
				Class klass = namespaceToImportFrom.getClasses().get(itemName);
				if (klass != null) {
					type = new TClass(klass);
				} else {
					Function function = namespaceToImportFrom.getFunctions()
							.get(itemName);
					if (function != null) {
						type = new TFunction(function);
					}

					// TODO: The target of the 'from foo import bar' can
					// be a variable.
				}
			}
		}

		if (type != null) {
			importInto(importReceiver, type, asName);
		}
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
	private TImportable simulateLoad(List<String> importPath,
			Package relativeToPackage) {
		TImportable loaded = simulateRelativeLoad(importPath, relativeToPackage);

		if (loaded == null)
			loaded = simulateRelativeLoad(importPath, topLevel);

		if (loaded == null)
			loaded = new TUnresolvedImport(importPath, relativeToPackage);

		return loaded;
	}

	/**
	 * Try to load a module or package looking <em>exclusively</em> at the parts
	 * of the model below {@code relativeToPackage}.
	 * 
	 * @param importPath
	 *            Path to search for relative to root, {@code relativeToPackage}
	 *            .
	 * @param relativeToPackage
	 *            Root of search.
	 * @return {@link Module} or {@link Package} loaded into a {@link Type} if
	 *         loading succeeded, {@code null} otherwise.
	 */
	private TImportable simulateRelativeLoad(List<String> importPath,
			Package relativeToPackage) {
		Package resolvedPackage = relativeToPackage.lookupPackage(importPath);
		if (resolvedPackage != null)
			return new TPackage(resolvedPackage);

		Module loadedModule = relativeToPackage.lookupModule(importPath);
		if (loadedModule != null)
			return new TModule(loadedModule);

		return null;
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

	protected abstract void importInto(Namespace scope, Type loadedImportable,
			String as);
}