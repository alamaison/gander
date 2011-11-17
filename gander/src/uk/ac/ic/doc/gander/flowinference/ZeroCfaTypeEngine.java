package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.dda.GoalSolver;
import uk.ac.ic.doc.gander.flowinference.dda.KnowledgeBase;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Namespace;

interface TypeEngine {

	/**
	 * Infer the type of the expression.
	 * 
	 * TODO: Can we get rid of the scope parameter? It shouldn't strictly be
	 * necessary.
	 */
	public TypeJudgement typeOf(exprType expression, Namespace scope);
}

/**
 * Flow-insensitive, context-insensitive, container-insensitive type inference
 * engine.
 */
public final class ZeroCfaTypeEngine implements TypeEngine {
	private KnowledgeBase blackboard = new KnowledgeBase();

	public ZeroCfaTypeEngine() {
	}

	public TypeJudgement typeOf(exprType expression, Namespace scope) {
		Goal<TypeJudgement> rootGoal = new ExpressionTypeGoal(scope, expression);
		System.out.print("Inferring type of " + expression);
		GoalSolver<TypeJudgement> solver = new GoalSolver<TypeJudgement>(
				rootGoal, blackboard);
		TypeJudgement j = solver.solve();
		System.out.println(" as " + j);
		return j;
	}
}
