package uk.ac.ic.doc.gander.flowinference.types;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Return;
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.VariableTypeGoal;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class FunctionReturnTypeSolver {

	private final SubgoalManager goalManager;
	private final InvokableCodeObject function;
	private final RedundancyEliminator<Type> returnTypes = new RedundancyEliminator<Type>();

	private boolean seenReturnStatement = false;

	FunctionReturnTypeSolver(SubgoalManager goalManager,
			InvokableCodeObject function) {
		this.goalManager = goalManager;
		this.function = function;

		try {
			function.codeBlock().accept(returnStatementSearcher());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (!returnTypes.isFinished() && !seenReturnStatement) {
			/*
			 * A missing 'return' statement means that the function returns
			 * builtin None.
			 */
			returnTypes.add(noneType(goalManager));
		}
	}

	Result<Type> solution() {

		return returnTypes.result();
	}

	private VisitorIF returnStatementSearcher() {
		return new LocalCodeBlockVisitor() {

			@Override
			public Object visitReturn(Return node) throws Exception {
				if (returnTypes.isFinished())
					return null;

				seenReturnStatement = true;

				if (node.value != null) {
					ModelSite<exprType> returnValue = new ModelSite<exprType>(
							node.value, function);
					ExpressionTypeGoal typer = new ExpressionTypeGoal(
							returnValue);
					returnTypes.add(goalManager.registerSubgoal(typer));
				} else {
					/*
					 * A bare 'return' statement means that the function returns
					 * builtin None.
					 */
					returnTypes.add(noneType(goalManager));
				}
				return null;
			}

			@Override
			protected Object unhandled_node(SimpleNode node) throws Exception {
				return null;
			}

			@Override
			public void traverse(SimpleNode node) throws Exception {
				// want all 'return' statements in code block
				if (!returnTypes.isFinished())
					node.traverse(this);
			}
		};
	}

	private Result<Type> noneType(SubgoalManager goalManager) {
		VariableTypeGoal typer = new VariableTypeGoal(new Variable("None",
				function));
		return goalManager.registerSubgoal(typer);
	}
}
