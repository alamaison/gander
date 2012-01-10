package uk.ac.ic.doc.gander.importing;

import java.util.LinkedList;
import java.util.List;

/**
 * Simulates the Python import mechanism.
 * 
 * This includes such complications as importing parent packages whenever a
 * child module or package is imported. Actually loading modules and packages,
 * and binding them to names isn't handled by this class. Instead it is left to
 * the loader and binder instances passed in the constructor.
 * 
 * The simulation is customised by implementing the loaded and binder to react
 * to two different aspects of the Python import mechanism. First, modules and
 * packages are loaded. The loader is given a path relative to a previously
 * loaded package but it is free to implement the loading operation however it
 * chooses. All that is required is that it returns a module representation if
 * the load succeeded or {@code null} if it fails. The second aspect is name
 * binding. The whole point of importing is to bind a name to a loaded module or
 * other namespace. The binder is free to interpret name binding however makes
 * sense for its task or even ignore it completely.
 * 
 * @param <O>
 *            the type of Java objects representing general Python objects that
 *            can be imported (including modules and other code objects)
 * 
 * @param <C>
 *            the type of Java objects representing Python code objects that
 *            could house an import statement in their code block (modules,
 *            functions, classes)
 * @param <M>
 *            the type of Java objects representing Python modules
 */
public final class ImportSimulator<O, C, M> {

	/**
	 * Callback through which the import simulation reports object being bound
	 * to names.
	 * 
	 * @param <O>
	 *            the type of Java objects representing general Python objects
	 *            that can be imported (including modules and other code
	 *            objects)
	 * 
	 * @param <C>
	 *            the type of Java objects representing Python code objects that
	 *            could house an import statement in their code block (modules,
	 *            functions, classes)
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
		 * @param importReceiver
		 *            the code object 'receiving' the effect of this binding;
		 *            not necessarily the code object whose namespace the
		 *            imported object is bound in as it may be a global name
		 */
		void bindModuleToLocalName(M loadedModule, String name, C importReceiver);

		void bindObjectToLocalName(O importedObject, String name,
				C importReceiver);

		void bindModuleToName(M loadedModule, String name, M receivingModule);

		void bindObjectToName(O importedObject, String name, M receivingModule);

		void onUnresolvedImport(Import<C, M> importInstance, String name);
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

		O loadNonModuleMember(String itemName, M sourceModule);
	}

	private final ImportSimulator.Binder<O, C, M> eventHandler;
	private final ImportSimulator.Loader<O, C, M> loader;

	public ImportSimulator(Binder<O, C, M> eventHandler, Loader<O, C, M> loader) {
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");
		if (loader == null)
			throw new NullPointerException("Must have an object loader");

		this.eventHandler = eventHandler;
		this.loader = loader;
	}

	/**
	 * Runs simulation for a given import instance.
	 * 
	 * Loads each segment of import path, binding it to a name with respect to
	 * previous segment.
	 * 
	 * @param importInstance
	 *            the kind of import being simulated
	 */
	public void simulateImport(Import<C, M> importInstance) {
		List<String> importPath = importInstance.specification().objectPath();
		BindingScheme<M> binder = importInstance.newBindingScheme(eventHandler,
				loader);

		M previouslyLoadedModule = null;
		List<String> processed = new LinkedList<String>();

		for (int i = 0; i < importPath.size(); ++i) {
			String token = importPath.get(i);

			processed.add(token);
			M module = simulateTwoStepModuleLoad(processed, importInstance
					.relativeTo());

			if (i == 0) {
				assert previouslyLoadedModule == null;
				if (importPath.size() == 1) {
					binder.bindSolitaryToken(module, token);
				} else {
					binder.bindFirstToken(module, token);
				}
			} else if (i < importPath.size() - 1) {
				assert previouslyLoadedModule != null;
				binder.bindIntermediateToken(module, token,
						previouslyLoadedModule);
			} else {
				assert i == importPath.size() - 1;
				assert previouslyLoadedModule != null;
				binder.bindFinalToken(module, token, previouslyLoadedModule);
			}

			if (module == null) {
				break; // abort import
			}

			previouslyLoadedModule = module;
		}
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
}
