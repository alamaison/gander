package uk.ac.ic.doc.gander.flowinference.typegoals.attribute;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.typegoals.TypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class AttributeTypeGoal implements TypeGoal {

	private final ModelSite<Attribute> attribute;

	public AttributeTypeGoal(ModelSite<Attribute> attribute) {
		this.attribute = attribute;
	}

	@Override
	public Result<PyObject> initialSolution() {
		return FiniteResult.bottom();
	}

	@Override
	public Result<PyObject> recalculateSolution(final SubgoalManager goalManager) {
		ModelSite<exprType> lhs = new ModelSite<exprType>(
				attribute.astNode().value, attribute.codeObject());
		ExpressionTypeGoal typer = new ExpressionTypeGoal(lhs);
		Result<PyObject> targetTypes = goalManager.registerSubgoal(typer);

		final String attributeName = ((NameTok) attribute.astNode().attr).id;

		Concentrator<PyObject, PyObject> processor = Concentrator.newInstance(
				new DatumProcessor<PyObject, PyObject>() {

					@Override
					public Result<PyObject> process(PyObject targetType) {
						return targetType
								.memberType(attributeName, goalManager);
					}
				}, TopT.INSTANCE);
		targetTypes.actOnResult(processor);

		return processor.result();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeTypeGoal other = (AttributeTypeGoal) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeTypeGoal [attribute=" + attribute + "]";
	}

}
