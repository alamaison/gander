package uk.ac.ic.doc.gander.model.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.model.Loadable;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.Package;

/**
 * This class loads a module but also follows any import statements and loads
 * their targets as well.
 */
class ImportAwareLoadablePopulator extends LoadablePopulator {

	private MutableModel model;

	ImportAwareLoadablePopulator(Loadable loadable, MutableModel model) {
		super(loadable);
		this.model = model;
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		ImportSimulator simulator = new Importer(getScope(), model);

		for (aliasType alias : node.names) {
			if (alias.asname == null) {
				simulator.simulateImport(((NameTok) alias.name).id);
			} else {
				simulator.simulateImportAs(((NameTok) alias.name).id,
						((NameTok) alias.asname).id);
			}
		}
		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {
		ImportSimulator simulator = new Importer(getScope(), model);

		for (aliasType alias : node.names) {
			if (alias.asname == null) {
				simulator.simulateImportFrom(((NameTok) node.module).id,
						((NameTok) alias.name).id);
			} else {
				simulator.simulateImportFromAs(((NameTok) node.module).id,
						((NameTok) alias.name).id, ((NameTok) alias.asname).id);
			}
		}
		return null;
	}

	private static class Importer extends ImportSimulator {

		private MutableModel model;

		public Importer(Namespace scope, MutableModel model) {
			super(scope, model.getTopLevelPackage());
			this.model = model;
		}

		@Override
		protected void bindName(Namespace scope, Namespace loadedImportable,
				String as) {
			// This is called by the simulator to instruct us to make whatever
			// changes are necessary for a name to be considered imported into
			// the given scope. We do nothing here because we aren't interested
			// in modifying the contents of namespaces - we just want to load
			// the necessary modules
			// FIXME: This explanation is clear as mud
		}

		/**
		 * Load a module or package looking <em>exclusively</em> at the parts of
		 * the model below {@code relativeToPackage}.
		 * 
		 * @param importPath
		 *            Path to search for relative to root, {@code
		 *            relativeToPackage} .
		 * @param relativeToPackage
		 *            Root of search.
		 * @return {@link Module} or {@link Package} if loading succeeded,
		 *         {@code null} otherwise.
		 * @throws Exception
		 */
		protected Loadable simulateLoad(List<String> importPath,
				Package relativeToPackage) {
			List<String> name = new ArrayList<String>(DottedName
					.toImportTokens(relativeToPackage.getFullName()));
			name.addAll(importPath);

			Loadable loaded = null;
			try {
				loaded = model.loadPackage(name);
				if (loaded == null)
					loaded = model.loadModule(name);
				// ignore exceptions as parse errors should be treated the same
				// way as any other unresolved import; by returning null
			} catch (ParseException e) {
			} catch (IOException e) {
			}

			return loaded;
		}

		@Override
		protected void onUnresolvedImport(List<String> importPath,
				Package relativeToPackage, Namespace importReceiver, String as) {
		}

		@Override
		protected void onUnresolvedImportFrom(List<String> fromPath,
				String itemName, Package relativeToPackage,
				Namespace importReceiver, String as) {
		}
	}
}