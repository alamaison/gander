package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;

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
public final class DefaultImportSimulator<O, C, M> {

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

		void onUnresolvedImport(List<String> importPath, M relativeTo,
				String as, M importReceiver);

		void onUnresolvedLocalImport(List<String> importPath, M relativeTo,
				String as, C importReceiver);
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

	private final DefaultImportSimulatorCore<O, C, M> core;

	public DefaultImportSimulator(Binder<O, C, M> eventHandler,
			Loader<O, C, M> loader) {
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");
		if (loader == null)
			throw new NullPointerException("Must have an object loader");

		this.core = new DefaultImportSimulatorCore<O, C, M>(eventHandler,
				loader);
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
	public void simulateImport(String importName, C importReceiver, M relativeTo) {
		assert !importReceiver.equals(relativeTo);

		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		core.simulateImport(tokens, importReceiver, relativeTo);
	}

	public void simulateImportAs(String importName, String asName,
			C importReceiver, M relativeTo) {
		assert !importReceiver.equals(relativeTo);

		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(importName));

		core.simulateImportAs(tokens, importReceiver, relativeTo, asName);
	}

	public void simulateImportFrom(String fromName, String itemName,
			C importReceiver, M relativeTo) {
		assert !importReceiver.equals(relativeTo);

		simulateImportFromAs(fromName, itemName, itemName, importReceiver,
				relativeTo);
	}

	public void simulateImportFromAs(String fromName, String itemName,
			String asName, C importReceiver, M relativeTo) {
		assert !importReceiver.equals(relativeTo);

		List<String> tokens = new LinkedList<String>(DottedName
				.toImportTokens(fromName));

		core.simulateImportFromAs(tokens, itemName, importReceiver, relativeTo,
				asName);
	}
}

final class DefaultImportSimulatorCore<O, C, M> {

	public static final class ImportBinding<O> {
		private final O importedObject;
		private final String boundToName;

		public O importedObject() {
			return importedObject;
		}

		public String importedAs() {
			return boundToName;
		}

		private ImportBinding(O importedObject, String boundToName) {
			this.importedObject = importedObject;
			this.boundToName = boundToName;
		}
	}

	private final DefaultImportSimulator.Binder<O, C, M> eventHandler;
	private final DefaultImportSimulator.Loader<O, C, M> loader;

	private final class XXXErrorWrapper implements
			DefaultImportSimulator.Binder<O, C, M> {

		private final DefaultImportSimulator.Binder<O, C, M> inner;

		public XXXErrorWrapper(
				DefaultImportSimulator.Binder<O, C, M> eventHandler) {
			this.inner = eventHandler;
		}

		public void bindModuleToLocalName(M loadedModule, String name,
				C importReceiver) {
			if (loadedModule != null) {
				inner.bindModuleToLocalName(loadedModule, name, importReceiver);
			} else {
				onUnresolvedLocalImport(Collections.<String> emptyList(), null,
						name, importReceiver);
			}
		}

		public void bindModuleToName(M loadedModule, String name,
				M receivingModule) {
			if (loadedModule != null) {
				inner.bindModuleToName(loadedModule, name, receivingModule);
			} else {
				onUnresolvedImport(Collections.<String> emptyList(), null,
						name, receivingModule);
			}
		}

		public void bindObjectToLocalName(O importedObject, String name,
				C importReceiver) {
			if (importedObject != null) {
				inner.bindObjectToLocalName(importedObject, name,
						importReceiver);
			} else {
				onUnresolvedLocalImport(Collections.<String> emptyList(), null,
						name, importReceiver);
			}
		}

		public void bindObjectToName(O importedObject, String name,
				M receivingModule) {
			if (importedObject != null) {
				inner.bindObjectToName(importedObject, name, receivingModule);
			} else {
				onUnresolvedImport(Collections.<String> emptyList(), null,
						name, receivingModule);
			}
		}

		public void onUnresolvedLocalImport(List<String> importPath,
				M relativeTo, String as, C importReceiver) {
			inner.onUnresolvedLocalImport(importPath, relativeTo, as,
					importReceiver);
		}

		public void onUnresolvedImport(List<String> importPath, M relativeTo,
				String as, M receivingModule) {
			inner.onUnresolvedImport(importPath, relativeTo, as,
					receivingModule);
		}

	}

	public DefaultImportSimulatorCore(
			DefaultImportSimulator.Binder<O, C, M> eventHandler,
			DefaultImportSimulator.Loader<O, C, M> loader) {
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");
		if (loader == null)
			throw new NullPointerException("Must have an object loader");

		this.eventHandler = new XXXErrorWrapper(eventHandler);
		this.loader = loader;
	}

	private static interface BindingScheme<O, M> {
		void bindSolitaryToken(O object, String name);

		void bindFirstToken(O object, String name);

		void bindIntermediateToken(O object, String name,
				M previouslyLoadedModule);

		void bindFinalToken(O object, String name, M previouslyLoadedModule);
	}

	private final class ImportScheme implements BindingScheme<M, M> {

		private final C outerImportReceiver;

		ImportScheme(C outerImportReceiver) {
			assert outerImportReceiver != null;
			this.outerImportReceiver = outerImportReceiver;
		}

		public void bindSolitaryToken(M module, String name) {
			bindFirstToken(module, name);
		}

		public void bindFirstToken(M module, String name) {
			eventHandler.bindModuleToLocalName(module, name,
					outerImportReceiver);
		}

		public void bindFinalToken(M module, String name,
				M previouslyLoadedModule) {
			eventHandler.bindModuleToName(module, name, previouslyLoadedModule);
		}

		public void bindIntermediateToken(M module, String name,
				M previouslyLoadedModule) {
			eventHandler.bindModuleToName(module, name, previouslyLoadedModule);
		}

	}

	private final class ImportAsScheme implements BindingScheme<M, M> {

		private final C outerImportReceiver;
		private final String asName;

		ImportAsScheme(C outerImportReceiver, String asName) {
			assert outerImportReceiver != null;
			assert !asName.isEmpty();
			this.outerImportReceiver = outerImportReceiver;
			this.asName = asName;
		}

		public void bindSolitaryToken(M module, String name) {
			eventHandler.bindModuleToLocalName(module, asName,
					outerImportReceiver);
		}

		public void bindFirstToken(M module, String name) {
		}

		public void bindIntermediateToken(M module, String name,
				M previouslyLoadedModule) {
			eventHandler.bindModuleToName(module, name, previouslyLoadedModule);
		}

		public void bindFinalToken(M module, String name,
				M previouslyLoadedModule) {
			eventHandler.bindModuleToName(module, name, previouslyLoadedModule);
			eventHandler.bindModuleToLocalName(module, asName,
					outerImportReceiver);
		}

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
	void simulateImport(List<String> importPath, C outerImportReceiver,
			M relativeTo) {
		simulateImportHelper(importPath, relativeTo, new ImportScheme(
				outerImportReceiver));
	}

	void simulateImportAs(List<String> importPath, C outerImportReceiver,
			M relativeTo, String as) {
		simulateImportHelper(importPath, relativeTo, new ImportAsScheme(
				outerImportReceiver, as));
	}

	private final class FromImportAsScheme implements BindingScheme<M, M> {

		private final C outerImportReceiver;
		private final String asName;

		FromImportAsScheme(C outerImportReceiver, String asName) {
			assert outerImportReceiver != null;
			assert !asName.isEmpty();
			this.outerImportReceiver = outerImportReceiver;
			this.asName = asName;
		}

		public void bindSolitaryToken(M module, String name) {
			bindFirstToken(module, name);
		}

		public void bindFirstToken(M object, String name) {
		}

		public void bindIntermediateToken(M object, String name,
				M previouslyLoadedModule) {
			eventHandler.bindModuleToName(object, name, previouslyLoadedModule);
		}

		public void bindFinalToken(M module, String name,
				M previouslyLoadedModule) {
			/*
			 * Resolve item name to an item relative the loaded module. If the
			 * item is a module it will have been loaded and passed to us here.
			 * Otherwise we try and find an item answering to that name.
			 */
			if (module == null) {
				O object = loader.loadNonModuleMember(name,
						previouslyLoadedModule);
				eventHandler.bindObjectToLocalName(object, asName,
						outerImportReceiver);
			} else {
				eventHandler.bindModuleToLocalName(module, asName,
						outerImportReceiver);
			}
		}

	}

	void simulateImportFromAs(List<String> fromPath, String itemName,
			C outerImportReceiver, M relativeTo, String asName) {

		List<String> itemPath = new ArrayList<String>(fromPath);
		itemPath.add(itemName);

		simulateImportHelper(itemPath, relativeTo, new FromImportAsScheme(
				outerImportReceiver, asName));
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
	private void simulateImportHelper(List<String> importPath, M relativeTo,
			BindingScheme<M, M> binder) {

		M previouslyLoadedModule = null;
		List<String> processed = new LinkedList<String>();

		for (int i = 0; i < importPath.size(); ++i) {
			String token = importPath.get(i);

			processed.add(token);
			M module = simulateTwoStepModuleLoad(processed, relativeTo);

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
