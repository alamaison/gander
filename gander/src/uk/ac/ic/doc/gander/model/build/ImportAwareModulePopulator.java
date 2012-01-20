package uk.ac.ic.doc.gander.model.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.importing.ImportFactory;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * This class loads a module but also follows any import statements and loads
 * their targets as well.
 */
class ImportAwareModulePopulator extends ModulePopulator {

	private MutableModel model;

	ImportAwareModulePopulator(Module loadable, MutableModel model) {
		super(loadable, model);
		this.model = model;
	}

	private Module parentModule(Namespace importReceiver) {
		if (importReceiver instanceof Module)
			return ((Module) importReceiver).getParent();
		else
			return importReceiver.getGlobalNamespace();
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		ImportSimulator<Member, Namespace, Module> simulator = new ImportSimulator<Member, Namespace, Module>(
				new DoNothingBinder(), new Importer(model));

		for (uk.ac.ic.doc.gander.importing.Import<Member, Namespace, Module> importInstance : ImportFactory
				.<Member, Namespace, Module> fromAstNode(node,
						parentModule(getScope()), getScope())) {
			simulator.simulateImport(importInstance);
		}
		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {
		ImportSimulator<Member, Namespace, Module> simulator = new ImportSimulator<Member, Namespace, Module>(
				new DoNothingBinder(), new Importer(model));

		for (uk.ac.ic.doc.gander.importing.Import<Member, Namespace, Module> importInstance : ImportFactory
				.<Member, Namespace, Module> fromAstNode(node,
						parentModule(getScope()), getScope())) {
			simulator.simulateImport(importInstance);
		}
		return null;
	}

	private static class Importer implements
			ImportSimulator.Loader<Member, Module> {

		private MutableModel model;

		public Importer(MutableModel model) {
			this.model = model;
		}

		/**
		 * Load a module or package looking <em>exclusively</em> at the parts of
		 * the model below {@code relativeToPackage}.
		 * 
		 * @param importPath
		 *            Path to search for relative to root, {@code
		 *            relativeToPackage}.
		 * @param relativeToModule
		 *            Root of search.
		 * @return {@link SourceFile} if loading succeeded, {@code null}
		 *         otherwise.
		 */
		public Module loadModule(List<String> importPath,
				Module relativeToModule) {
			List<String> name = new ArrayList<String>(DottedName
					.toImportTokens(relativeToModule.getFullName()));
			name.addAll(importPath);

			return loadModule(name);
		}

		/**
		 * Load a module or package.
		 * 
		 * @param importPath
		 *            Path to search for relative to top level.
		 * @return {@link SourceFile} if loading succeeded, {@code null}
		 *         otherwise.
		 */
		public Module loadModule(List<String> importPath) {

			Module loaded = null;
			try {
				loaded = model.loadPackage(importPath);
				if (loaded == null)
					loaded = model.loadModule(importPath);
				// ignore exceptions as parse errors should be treated the same
				// way as any other unresolved import; by returning null
			} catch (ParseException e) {
				System.err.println("Parse error in "
						+ DottedName.toDottedName(importPath));
			} catch (IOException e) {
			}

			return loaded;
		}

		public Member loadNonModuleMember(String itemName, Module sourceModule) {
			return sourceModule.lookupMember(itemName);
		}
	}

	/**
	 * No-op binding handler.
	 * 
	 * The methods of this class are called by the simulator to instruct us to
	 * make whatever changes are necessary for a name to be considered imported
	 * into the given scope. We do nothing here because we aren't interested in
	 * modifying the contents of namespaces - we just want to load the necessary
	 * modules.
	 * 
	 * FIXME: This explanation is clear as mud
	 */
	private static class DoNothingBinder implements
			ImportSimulator.Binder<Member, Namespace, Module> {

		public void bindModuleToLocalName(Module loadedModule, String name,
				Namespace container) {
		}

		public void bindModuleToName(Module loadedModule, String name,
				Module receivingModule) {
		}

		public void bindObjectToLocalName(Member importedObject, String name,
				Namespace container) {
		}

		public void bindObjectToName(Member importedObject, String name,
				Module receivingModule) {
		}

		public void onUnresolvedImport(
				uk.ac.ic.doc.gander.importing.Import<Member, Namespace, Module> importInstance,
				String name, Module receivingModule) {
		}

		public void onUnresolvedLocalImport(
				uk.ac.ic.doc.gander.importing.Import<Member, Namespace, Module> importInstance,
				String name) {
			// FIXME: This gets fired when 'from x import a' occurs inside a
			// module that x is currently importing as x is not yet complete.
			// That's fine, that's what's supposed to happen. However, it also
			// happens when the import statement in x is couched in a function
			// or class def that occurs before the definition of a. This
			// shouldn't happen as in the real Python interpreter the module has
			// been fully constructed by the time the function/class bodies are
			// executed, but we combine the two things and do it depth-first
			// (like the AST) causing this problem.
			//
			// It doesn't really matter at the moment as the model doesn't care
			// if the from-style imports can't find their 'item' (the model
			// never does anything with the item when it's a function, class or
			// variable; and it always will be one of those three in this case
			// as submodules/packages are loaded on demand when their import is
			// encountered - they aren't affected by being part way through
			// building their parent) but its worth bearing in mind for the
			// future
		}
	}
}