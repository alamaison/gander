package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TUnresolvedImport implements TCodeObject {

	private Import<?, ?, ?> importInstance;

	public TUnresolvedImport(Import<?, ?, ?> importInstance) {
		if (importInstance == null)
			throw new NullPointerException("Failed import not optional");
		this.importInstance = importInstance;
	}

	public CodeObject codeObject() {
		return null;
	}

	public String getName() {
		return "<unresolved import: '" + importInstance + "'>";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on an unresolved import cannot be accurately typed so we
	 * approximate it conservatively as Top.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

}
