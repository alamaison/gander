package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
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
 * 
 * @param <O>
 *            the supertype of Java objects representing other Python objects
 *            that can be imported
 * 
 * @param <C>
 *            the type of Java objects representing Python code objects
 *            (modules, functions, classes)
 * @param <M>
 *            the type of Java objects representing Python modules
 */
public final class DefaultImportSimulator<O, C extends O, M extends C>
		implements ImportSimulator {

	/**
	 * Callback through which the import simulation reports object being bound
	 * to names.
	 * 
	 * @param <O>
	 *            the supertype of Java objects representing other Python
	 *            objects that can be imported
	 * 
	 * @param <C>
	 *            the type of Java objects representing Python code objects
	 *            (modules, functions, classes)
	 * @param <M>
	 *            the type of Java objects representing Python modules
	 */
	public interface Binder<O, C, M> {

		/**
		 * The simulation is reporting that an object would be bound to a name
		 * in a Python namespace.
		 * 
		 * Note this function is not passed the namespace that the name binds
		 * in. This is left to the implementation to resolve. It will usually be
		 * the namespace associated with the import location but, if the
		 * location includes the global keyword, it may be the global keyword.
		 * 
		 * @param loadedObject
		 *            the representation of the object being bound to a name
		 * @param name
		 *            the name the object is bound to
		 * @param codeBlock
		 *            the code object 'receiving' the effect of this binding;
		 *            not necessarily the code object whose namespace the
		 *            imported object is bound in as it may be a global name
		 */
		void bindName(O loadedObject, String name, C importReceiver);

		void onUnresolvedImport(List<String> importPath, M relativeTo,
				String as, C codeBlock);

		void onUnresolvedImportFromItem(List<String> fromPath, M relativeTo,
				String itemName, String as, C codeBlock);

	}

	/**
	 * Callback through which a specific system model is presented to the import
	 * simulation.
	 * 
	 * @param <O>
	 *            the supertype of Java objects representing other Python
	 *            objects that can be imported
	 * 
	 * @param <C>
	 *            the type of Java objects representing Python code objects
	 *            (modules, functions, classes)
	 * @param <M>
	 *            the type of Java objects representing Python modules
	 */
	public interface Loader<O, C, M> {

		/**
		 * Load a module or package relative to the given module.
		 * 
		 * If loading fails, return {@code null}.
		 * 
		 * @param importPath
		 *            path of module to load; relative to the given module
		 * @param relativeToModule
		 *            representation of module to load relative to
		 * 
		 * @return An object representing the loaded module or package if
		 *         loading succeeded, {@code null} otherwise.
		 */
		M loadModule(List<String> importPath, M relativeToModule);

		/**
		 * Load a module or package relative to the top level.
		 * 
		 * If loading fails, return {@code null}.
		 * 
		 * @param importPath
		 *            absolute path of module to load
		 * 
		 * @return An object representing the loaded module or package if
		 *         loading succeeded, {@code null} otherwise.
		 */
		M loadModule(List<String> importPath);

		O loadNonModuleMember(String itemName,
				C codeObjectWhoseNamespaceWeAreLoadingFrom);

		M parentModule(C importReceiver);

	}

	private final C importReceiver;
	private final DefaultImportSimulatorCore<O, C, M> core;
	private final M relativeTo;

	/**
	 * Note, importReceiver may not actually be the import receiver. It depends
	 * on the binding scope of 'as' in importReceiver. It could be the global
	 * scope.
	 */
	public DefaultImportSimulator(C importReceiver,
			Binder<O, C, M> eventHandler, Loader<O, C, M> loader) {
		if (importReceiver == null)
			throw new NullPointerException("Must specify where import occurs");
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");
		if (loader == null)
			throw new NullPointerException("Must have an object loader");

		this.core = new DefaultImportSimulatorCore<O, C, M>(eventHandler,
				loader);

		this.importReceiver = importReceiver;
		this.relativeTo = loader.parentModule(importReceiver);
		assert !importReceiver.equals(this.relativeTo);
	}

	/**
	 * Import a module as in {@code import foo.bar.baz}.
	 * 
	 * Binds the module object representation named by the first token in the
	 * dotted import path to a matching name in the namespace this object was
	 * initialised with. The it binds the modules named by any subsequent tokens
	 * to their matching names in each previously loaded module's namespace.
	 * 
	 * For example, when importing {@code x.y.z}, Python will import {@code z}
	 * into {@code y} and {@code y} into {@code x}. Finally the module y is
	 * bound to the name {@code x} in the binding namespace for {@code x}
	 * relative to the given code block.
	 */
	public void simulateImport(String importName) {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		core.simulateImport(tokens, importReceiver, relativeTo);
	}

	public void simulateImportAs(String importName, String asName) {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		core.simulateImportAs(tokens, importReceiver, relativeTo, asName);
	}

	public void simulateImportFrom(String fromName, String itemName) {
		simulateImportFromAs(fromName, itemName, itemName);
	}

	public void simulateImportFromAs(String fromName, String itemName,
			String asName) {
		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(fromName));

		core.simulateImportFromAs(tokens, itemName, importReceiver, relativeTo,
				asName);
	}
}

final class DefaultImportSimulatorCore<O, C extends O, M extends C> {

	private final DefaultImportSimulator.Binder<O, C, M> eventHandler;
	private final DefaultImportSimulator.Loader<O, C, M> loader;

	/**
	 * FIXME: importReceiver may not actually be the import receiver. It depends
	 * on the binding scope of 'as' in importReceiver. It could be the global
	 * scope.
	 */
	public DefaultImportSimulatorCore(
			DefaultImportSimulator.Binder<O, C, M> eventHandler,
			DefaultImportSimulator.Loader<O, C, M> loader) {
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");
		if (loader == null)
			throw new NullPointerException("Must have an object loader");

		this.eventHandler = eventHandler;
		this.loader = loader;
	}

	/**
	 * Import a module as in {@code import foo.bar.baz}.
	 * 
	 * Binds the module object representation named by the first token in the
	 * dotted import path to a matching name in the namespace this object was
	 * initialised with. The it binds the modules named by any subsequent tokens
	 * to their matching names in each previously loaded module's namespace.
	 * 
	 * For example, when importing {@code x.y.z}, Python will import {@code z}
	 * into {@code y} and {@code y} into {@code x}. Finally the module y is
	 * bound to the name {@code x} in the binding namespace for {@code x}
	 * relative to the given code block.
	 */
	void simulateImport(List<String> importPath, C importReceiver, M relativeTo) {
		simulateImportHelper(importPath, relativeTo, importReceiver);
	}

	void simulateImportAs(List<String> importPath, C importReceiver,
			M relativeTo, String as) {
		C loaded = simulateImportHelper(importPath, relativeTo, null);
		handleBind(importPath, relativeTo, importReceiver, as, loaded);
	}

	void simulateImportFromAs(List<String> fromPath, String itemName,
			C importReceiver, M relativeTo, String asName) {

		C codeObjectWhoseNamespaceWeAreLoadingFrom = simulateImportHelper(
				fromPath, relativeTo, null);
		if (codeObjectWhoseNamespaceWeAreLoadingFrom == null)
			// Reporting this failure is handled by simulateImportHelper
			return;

		List<String> itemPath = new ArrayList<String>(fromPath);
		itemPath.add(itemName);

		// Resolve item name to an item inside the namespace. If the item is a
		// module we have to load it, otherwise we can just investigate it
		O loaded = simulateTwoStepModuleLoad(itemPath, relativeTo);
		if (loaded == null) {
			loaded = loader.loadNonModuleMember(itemName,
					codeObjectWhoseNamespaceWeAreLoadingFrom);
		}

		handleBindFrom(fromPath, itemName, relativeTo, importReceiver, asName,
				loaded);
	}

	/**
	 * Load each segment of import path, binding it to a name with respect to
	 * previous segment.
	 * 
	 * @param importPath
	 *            the import path being loaded
	 * @param relativeTo
	 *            the module that the import path is relative to
	 * @param initialImportReceiver
	 *            the object that the first segment is bound with respect to;
	 *            optional; {@code import a.b.c} does not want this as {@code c}
	 *            , rather than {@code a} gets bound with respect to that object
	 * 
	 * @return the object loaded by the <em>final</em> segment; this allows
	 *         {@code import a.b.c as x} to bind {@code c} in the initial import
	 *         receiver rather than {@code a} and the from-style imports look in
	 *         it to find their item.
	 */
	private M simulateImportHelper(List<String> importPath, M relativeTo,
			C initialImportReceiver) {

		C importReceiver = initialImportReceiver;
		List<String> processed = new LinkedList<String>();
		M loaded = null;
		for (String token : importPath) {
			processed.add(token);
			loaded = simulateTwoStepModuleLoad(processed, relativeTo);
			if (loaded != null) {
				if (importReceiver != null)
					eventHandler.bindName(loaded, token, importReceiver);
			} else {
				eventHandler.onUnresolvedImport(importPath, relativeTo, token,
						importReceiver);
				break; // abort import
			}
			importReceiver = loaded;
		}

		return loaded;
	}

	/**
	 * Try to load a module. As in Python, this attempts to load it relative to
	 * the given package, {@code relativeTo}, and if this fails attempts
	 * relative to the top-level package.
	 * 
	 * If neither of these succeeds it returns null to indicate import
	 * resolution failed.
	 * 
	 * @param importPath
	 *            the import path of the module; either relative or absolute
	 * @param relativeTo
	 *            the module that the first import attempt is relative to
	 * @return an object representing the loaded module if loading succeeded;
	 *         {@code null} otherwise
	 */
	private M simulateTwoStepModuleLoad(List<String> importPath, M relativeTo) {
		M loaded = null;

		if (relativeTo != null)
			loaded = loader.loadModule(importPath, relativeTo);

		if (loaded == null)
			loaded = loader.loadModule(importPath);

		return loaded;
	}

	private void handleBind(List<String> importPath, M relativeTo,
			C importReceiver, String as, C loaded) {
		if (loaded != null)
			eventHandler.bindName(loaded, as, importReceiver);
		else
			eventHandler.onUnresolvedImport(importPath, relativeTo, as,
					importReceiver);
	}

	private void handleBindFrom(List<String> fromPath, String itemName,
			M relativeTo, C importReceiver, String as, O loaded) {
		if (loaded != null)
			eventHandler.bindName(loaded, as, importReceiver);
		else
			eventHandler.onUnresolvedImportFromItem(fromPath, relativeTo,
					itemName, as, importReceiver);
	}
}
