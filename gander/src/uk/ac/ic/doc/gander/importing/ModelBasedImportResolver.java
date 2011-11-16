package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Simulator for Python import mechanisms relying on model items being
 * preloaded.
 */
public abstract class ModelBasedImportResolver extends ImportSimulator {

	private final Model model;

	protected ModelBasedImportResolver(Model model, Namespace importReceiver) {
		super(importReceiver);

		assert model != null;
		this.model = model;
	}

	@Override
	protected final Module simulateLoad(List<String> importPath,
			Module relativeToPackage) {
		List<String> name = new ArrayList<String>(DottedName
				.toImportTokens(relativeToPackage.getFullName()));
		name.addAll(importPath);

		return simulateLoad(name);
	}

	@Override
	protected Module simulateLoad(List<String> importPath) {

		// The imported module/package will always exist in the model
		// already if it exists (on disk) at all as the model must have
		// tried to import it already. Therefore we only do a lookup here
		// rather than attempting a load.
		return model.lookup(importPath);
	}
}