package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopF;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public class TUnresolvedImport implements TCodeObject {

	private final Import<?, ?> importInstance;

	public TUnresolvedImport(Import<?, ?> importInstance) {
		// if (importInstance == null)
		// throw new NullPointerException("Failed import not optional");
		this.importInstance = importInstance;
	}

	@Override
	public CodeObject codeObject() {
		return null;
	}

	@Override
	public String getName() {
		return "<unresolved import: '" + importInstance + "'>";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on an unresolved import cannot be accurately typed so we
	 * approximate it conservatively as Top.
	 */
	@Override
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Namespace> memberReadableNamespaces() {
		return Collections.<Namespace> singleton(dummyNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Namespace memberWriteableNamespace() {
		return dummyNamespace();
	}

	@Override
	public String toString() {
		return "TUnresolvedImport [importInstance=" + importInstance + "]";
	}

	private Namespace dummyNamespace() {
		return new Namespace() {

			@Override
			public Result<ModelSite<exprType>> references(
					SubgoalManager goalManager) {
				return TopF.INSTANCE;
			}

			@Override
			public Result<ModelSite<exprType>> writeableReferences(
					SubgoalManager goalManager) {
				return TopF.INSTANCE;
			}

			@Override
			public Set<Variable> variablesInScope(String name) {
				return Collections.emptySet();
			}

			@Override
			public Set<Variable> variablesWriteableInScope(String name) {
				return Collections.emptySet();
			}

			@Override
			public Model model() {
				return null;
			}
		};
	}

}
