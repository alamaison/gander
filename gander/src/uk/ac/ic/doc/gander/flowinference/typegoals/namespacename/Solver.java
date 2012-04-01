package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.ModuleNamespace;
import uk.ac.ic.doc.gander.model.NamespaceName;

final class NamespaceNameTypeGoalSolver {

	private final NamespaceName name;
	private final SubgoalManager goalManager;

	private final RedundancyEliminator<Type> completeType = new RedundancyEliminator<Type>();

	NamespaceNameTypeGoalSolver(NamespaceName name, SubgoalManager goalManager) {
		if (name == null)
			throw new NullPointerException(
					"Can't find an name's type if we don't have a name");
		if (goalManager == null)
			throw new NullPointerException(
					"We need to be able to issue subqueries");

		this.name = name;
		this.goalManager = goalManager;

		addTypesFromNamespace();

		if (name.namespace() instanceof Class) {
			// XXX: what about inherited objects? Their namespace is not class
			addTypesFromInheritanceChain((Class) name.namespace());
		}

		if (name.namespace() instanceof ModuleNamespace) {
			addTypesFromIntermediateModuleImport();
		}
	}

	Result<Type> solution() {
		return completeType.result();
	}

	private void addTypesFromNamespace() {

		completeType.add(new UnqualifiedNameDefinitionsPartialSolution(
				goalManager, name).partialSolution());

		if (!completeType.isFinished()) {
			completeType.add(new QualifiedNameDefinitionsPartialSolution(
					goalManager, name).partialSolution());
		}
	}

	private void addTypesFromInheritanceChain(Class klass) {

		for (exprType supertype : klass.inheritsFrom()) {

			/*
			 * Methods in a subclass override those declared in a superclass but
			 * we must flow both into the subclass's namespace as the subclass's
			 * version could be deleted leading to calls invoking the superclass
			 * version.
			 */

			Result<Type> supertypeTypes = goalManager
					.registerSubgoal(new ExpressionTypeGoal(
							new ModelSite<exprType>(supertype, klass
									.codeObject().parent())));

			MemberTyper memberTyper = new MemberTyper();
			supertypeTypes.actOnResult(memberTyper);
		}
	}

	/**
	 * Modules can be very sneakily bound to a token in another module by an
	 * import statement in a third module. This method catches those kinds of
	 * import.
	 */
	private void addTypesFromIntermediateModuleImport() {

		if (!completeType.isFinished()) {

			completeType.add(new FiniteResult<Type>(name.namespace().model()
					.importTable().explicitBindings(name)));
		}
	}

	private final class MemberTyper implements Processor<Type> {

		@Override
		public void processInfiniteResult() {
			// do nothing, no member found
		}

		@Override
		public void processFiniteResult(Set<Type> result) {
			for (Type supertypeType : result) {
				if (completeType.isFinished())
					break;

				completeType.add(supertypeType.memberType(name.name(),
						goalManager));
			}
		}
	}
}