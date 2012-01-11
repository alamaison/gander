package uk.ac.ic.doc.gander.importing;

import java.util.List;

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
@Deprecated
public final class LegacyImportSimulator<O, C extends O, M extends C> {

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
	@Deprecated
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
				String as, C importReceiver);

		void onUnresolvedImportFromItem(List<String> fromPath, M relativeTo,
				String itemName, String as, C importReceiver);

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
	@Deprecated
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
	private final ImportSimulator<O, C, M> inner;
	private final M relativeTo;

	/**
	 * Note, importReceiver may not actually be the import receiver. It depends
	 * on the binding scope of 'as' in importReceiver. It could be the global
	 * scope.
	 */
	public LegacyImportSimulator(C importReceiver,
			final Binder<O, C, M> eventHandler, final Loader<O, C, M> loader) {
		if (importReceiver == null)
			throw new NullPointerException("Must specify where import occurs");
		if (eventHandler == null)
			throw new NullPointerException(
					"Must have an event handler to react to import events");
		if (loader == null)
			throw new NullPointerException("Must have an object innerLoader");

		this.inner = new ImportSimulator<O, C, M>(new LegacyBinderAdaptor(
				eventHandler), new LegacyLoaderAdaptor(loader));

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

		inner.simulateImport(new StandardImport<O, C, M>(
				StandardImportSpecification.newInstance(importName),
				relativeTo, importReceiver));
	}

	public void simulateImportAs(String importName, String asName) {

		inner.simulateImport(new StandardImportAs<O, C, M>(
				StandardImportAsSpecification.newInstance(importName, asName),
				relativeTo, importReceiver));
	}

	public void simulateImportFrom(String fromName, String itemName) {

		inner.simulateImport(new FromImport<O, C, M>(FromImportSpecification
				.newInstance(fromName, itemName), relativeTo, importReceiver));
	}

	public void simulateImportFromAs(String fromName, String itemName,
			String asName) {

		inner.simulateImport(new FromImportAs<O, C, M>(
				FromImportAsSpecification.newInstance(fromName, itemName,
						asName), relativeTo, importReceiver));
	}

	private final class LegacyBinderAdaptor implements
			ImportSimulator.Binder<O, C, M> {

		private Binder<O, C, M> innerEventHandler;

		private LegacyBinderAdaptor(Binder<O, C, M> eventHandler) {
			assert eventHandler != null;
			this.innerEventHandler = eventHandler;
		}

		private void bindName(O loadedObject, String name, C importReceiver) {
			innerEventHandler.bindName(loadedObject, name, importReceiver);
		}

		public void bindModuleToLocalName(M loadedModule, String name,
				C importReceiver) {
			bindName(loadedModule, name, importReceiver);
		}

		public void bindModuleToName(M loadedModule, String name,
				M receivingModule) {
			bindName(loadedModule, name, receivingModule);
		}

		public void bindObjectToLocalName(O importedObject, String name,
				C importReceiver) {
			bindName(importedObject, name, importReceiver);
		}

		public void bindObjectToName(O importedObject, String name,
				M receivingModule) {
			bindName(importedObject, name, receivingModule);
		}

		public void onUnresolvedImport(Import<O, C, M> importInstance,
				String name, M receivingModule) {
			assert importInstance.container() != null;

			innerEventHandler.onUnresolvedImport(importInstance.specification()
					.objectPath(), importInstance.relativeTo(), name,
					receivingModule);
		}

		public void onUnresolvedLocalImport(Import<O, C, M> importInstance,
				String name) {
			assert importInstance.container() != null;

			innerEventHandler.onUnresolvedImport(importInstance.specification()
					.objectPath(), importInstance.relativeTo(), name,
					importInstance.container());
		}
	}

	private final class LegacyLoaderAdaptor implements
			ImportSimulator.Loader<O, M> {

		private final Loader<O, C, M> innerLoader;

		private LegacyLoaderAdaptor(Loader<O, C, M> loader) {
			this.innerLoader = loader;
		}

		public M loadModule(List<String> importPath, M relativeToModule) {
			return innerLoader.loadModule(importPath, relativeToModule);
		}

		public M loadModule(List<String> importPath) {
			return innerLoader.loadModule(importPath);
		}

		public O loadNonModuleMember(String itemName, M sourceModule) {
			return innerLoader.loadNonModuleMember(itemName, sourceModule);
		}
	}
}