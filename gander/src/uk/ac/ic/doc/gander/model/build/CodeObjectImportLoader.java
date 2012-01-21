package uk.ac.ic.doc.gander.model.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.importing.ImportFactory;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.codeobject.NamedCodeObject;

/**
 * Performs the 'elaboration phase' for a code object.
 * 
 * In other words, it simulates the import behaviour for any import statements
 * in and below the given code object.
 */
public class CodeObjectImportLoader {

	private MutableModel model;

	public CodeObjectImportLoader(CodeObject codeObject, MutableModel model) {
		this.model = model;
		CodeObjectWalker walker = new CodeObjectWalker() {

			@Override
			protected void visitCodeObject(CodeObject codeObject) {
				try {
					codeObject.codeBlock().accept(
							new CodeObjectImportVisitor(
									parentPackage(codeObject), codeObject));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		walker.walk(codeObject);
	}

	private ModuleCO parentPackage(CodeObject importContainer) {
		ModuleCO enclosingModule = importContainer.enclosingModule();
		// XXX: we're using the nasty old model to find the package
		Module yuk = enclosingModule.oldStyleConflatedNamespace().getParent();
		if (yuk != null) {
			return yuk.codeObject();
		} else {
			return null;
		}
	}

	private final class CodeObjectImportVisitor extends LocalCodeBlockVisitor {

		private final ModuleCO relativeToPackage;
		private final CodeObject importContainer;

		CodeObjectImportVisitor(ModuleCO relativeToPackage,
				CodeObject importContainer) {
			this.relativeToPackage = relativeToPackage;
			this.importContainer = importContainer;
		}

		@Override
		public Object visitImport(Import node) throws Exception {
			ImportSimulator<CodeObject, CodeObject, ModuleCO> simulator = new ImportSimulator<CodeObject, CodeObject, ModuleCO>(
					new DoNothingBinder<CodeObject, CodeObject, ModuleCO>(),
					new Importer(model));

			for (uk.ac.ic.doc.gander.importing.Import<CodeObject, CodeObject, ModuleCO> importInstance : ImportFactory
					.<CodeObject, CodeObject, ModuleCO> fromAstNode(node,
							relativeToPackage, importContainer)) {
				simulator.simulateImport(importInstance);
			}
			return null;
		}

		@Override
		public Object visitImportFrom(ImportFrom node) throws Exception {
			ImportSimulator<CodeObject, CodeObject, ModuleCO> simulator = new ImportSimulator<CodeObject, CodeObject, ModuleCO>(
					new DoNothingBinder<CodeObject, CodeObject, ModuleCO>(),
					new Importer(model));

			for (uk.ac.ic.doc.gander.importing.Import<CodeObject, CodeObject, ModuleCO> importInstance : ImportFactory
					.<CodeObject, CodeObject, ModuleCO> fromAstNode(node,
							relativeToPackage, importContainer)) {
				simulator.simulateImport(importInstance);
			}
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			// import statement can be nested in if/while/etc
			node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {

			return null;
		}

	}

	private static class Importer implements
			ImportSimulator.Loader<CodeObject, ModuleCO> {

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
		public ModuleCO loadModule(List<String> importPath,
				ModuleCO relativeToModule) {
			List<String> name = new ArrayList<String>(DottedName
					.toImportTokens(relativeToModule
							.oldStyleConflatedNamespace().getFullName()));
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
		public ModuleCO loadModule(List<String> importPath) {

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

			if (loaded != null) {
				return loaded.codeObject();
			} else {
				return null;
			}
		}

		public CodeObject loadNonModuleMember(String itemName,
				ModuleCO sourceModule) {
			/*
			 * We search the nested code objects in order checking their names
			 * and choose the _last_ one that matches. This sortof matches
			 * Python's behaviour as later code object declarations will replace
			 * the binding in the namespace so the last one will be imported in
			 * preference to the earlier declarations.
			 * 
			 * WARNING: This is still not right! This is why we call it the
			 * 'dodgy model'. Names can be added to the namespace in more ways
			 * than just declaration and these can be imported too.
			 */
			CodeObject object = null;
			for (CodeObject nestedCodeObject : sourceModule.nestedCodeObjects()) {
				if (nestedCodeObject instanceof NamedCodeObject) {
					if (((NamedCodeObject) nestedCodeObject).declaredName()
							.equals(itemName)) {
						object = nestedCodeObject;
					}
				}
			}

			return object;
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
	private static class DoNothingBinder<O, C, M> implements
			ImportSimulator.Binder<O, C, M> {

		public void bindModuleToLocalName(M loadedModule, String name,
				C container) {
		}

		public void bindModuleToName(M loadedModule, String name,
				M receivingModule) {
		}

		public void bindObjectToLocalName(O importedObject, String name,
				C container) {
		}

		public void bindObjectToName(O importedObject, String name,
				M receivingModule) {
		}

		public void onUnresolvedImport(
				uk.ac.ic.doc.gander.importing.Import<O, C, M> importInstance,
				String name, M receivingModule) {
		}

		public void onUnresolvedLocalImport(
				uk.ac.ic.doc.gander.importing.Import<O, C, M> importInstance,
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