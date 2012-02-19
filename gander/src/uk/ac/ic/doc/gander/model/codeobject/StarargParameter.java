package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Collections;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.argumentsType;

import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.KeywordArgument;
import uk.ac.ic.doc.gander.flowinference.PositionalArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class StarargParameter implements FormalParameter {

	private final ModelSite<argumentsType> argsNode;
	private final int starargIndex;

	public StarargParameter(ModelSite<argumentsType> argsNode, int starargIndex) {
		this.argsNode = argsNode;
		this.starargIndex = starargIndex;
	}

	@Override
	public ModelSite<NameTok> site() {
		return new ModelSite<NameTok>((NameTok) argsNode.astNode().vararg,
				argsNode.codeObject());
	}

	@Override
	public ArgumentPassage passage(PositionalArgument argument) {
		return passage();
	}

	@Override
	public ArgumentPassage passage(KeywordArgument argument) {
		return passage();
	}

	private ArgumentPassage passage() {

		return new ArgumentPassage() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return TopFp.INSTANCE;
			}
		};
	}

	@Override
	public Result<Type> typeAtCall(ModelSite<Call> callSite,
			SubgoalManager goalManager) {
		/*
		 * We don't need type inference for this - always a tuple.
		 */
		return new FiniteResult<Type>(Collections.singleton(new TObject(
				argsNode.model().builtinTuple())));
	}
}
