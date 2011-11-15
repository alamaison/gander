package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.HashSet;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.aliasType;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.ast.BindingStatementVisitor;
import uk.ac.ic.doc.gander.flowinference.ImportTyper;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Find conservative approximation of the types bound to a given name in a
 * particular code block.
 */
public final class BoundTypeGoal implements TypeGoal {
	private final Namespace enclosingScope;
	private final String name;
	private final Model model;

	public BoundTypeGoal(Model model, Namespace enclosingScope, String name) {
		this.model = model;
		this.enclosingScope = enclosingScope;
		this.name = name;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager manager) {

		return new BoundTypeVisitor(manager).getJudgement();
	}

	private class BoundTypeVisitor extends BindingStatementVisitor {
		private final HashSet<Type> boundTypes = new HashSet<Type>();
		private final SubgoalManager goalManager;

		/*
		 * Keep adding to boundTypes until this stops being null which indicates
		 * a judgement has been reached. Basically this doesn't happen until be
		 * decide on Top or finish processing everything and convert our set to
		 * a type judgement.
		 */
		private TypeJudgement judgement = null;

		BoundTypeVisitor(SubgoalManager goalManager) {
			this.goalManager = goalManager;

			TypeJudgement parameterType = goalManager
					.registerSubgoal(new ParameterTypeGoal(model,
							enclosingScope, name));
			if (parameterType instanceof SetBasedTypeJudgement) {
				boundTypes.addAll(((SetBasedTypeJudgement) parameterType)
						.getConstituentTypes());
			} else {
				judgement = new Top();
				return;
			}

			try {
				enclosingScope.asCodeBlock().accept(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (judgement == null) {
				judgement = new SetBasedTypeJudgement(boundTypes);
			}
		}

		public TypeJudgement getJudgement() {
			return judgement;
		}

		@Override
		public Object visitTryExcept(TryExcept node) throws Exception {
			if (judgement != null)
				return null;

			for (excepthandlerType handler : node.handlers) {
				if (handler.name instanceof Name) {
					if (((Name) handler.name).id.equals(name)) {

						/*
						 * If any of the above attempts to convert the declared
						 * type to a model class fail, we must add Top as we
						 * _have_ found the name, we just don't know its type.
						 * Not adding anything would mean we found no binding
						 * for that name which would be a lie.
						 */
						if (handler.type instanceof Name) {

							// XXX: Very bad! We're not trying to
							// resolve the type expression properly.
							// Instead we look blindly at the top level
							// hoping that any exception will be
							// declared there. Do this right.
							Class exceptionClass = model.getTopLevel()
									.getClasses().get(((Name) handler.type).id);
							if (exceptionClass != null) {
								boundTypes.add(new TClass(exceptionClass));
							} else {

								// Give up early because nothing beats Top
								judgement = new Top();
								return null;
							}
						} else {
							// TODO: Try to resolve the expression to an
							// exception class

							// Give up early because nothing beats Top
							judgement = new Top();
							return null;
						}

						// The exception handler could rebind the
						// exception so we must investigate its body.
						for (stmtType stmt : handler.body) {
							stmt.accept(this);
						}
					}
				} else {
					// XXX: No idea what happens here. How could the
					// name of the exception object _not_ be a name?
				}
			}

			// XXX: Is it possible for the try block to bind one of the
			// handler's exception objects before the handler is
			// reached? It would seem odd but the spec [PEP 227] seems
			// to imply it is. Anyway, let's look for the name in it
			// anyway.
			for (stmtType stmt : node.body) {
				stmt.accept(this);
			}

			return null;
		}

		@Override
		public Object visitImportFrom(ImportFrom node) throws Exception {
			for (aliasType alias : node.names) {
				if (alias.asname != null
						&& ((NameTok) alias.asname).id.equals(name)) {
					newImportResolver().simulateImportFromAs(
							((NameTok) node.module).id,
							((NameTok) alias.name).id,
							((NameTok) alias.asname).id);
				} else if (((NameTok) alias.name).id.equals(name)) {
					newImportResolver().simulateImportFrom(
							((NameTok) node.module).id,
							((NameTok) alias.name).id);
				}
			}

			return null;
		}

		@Override
		public Object visitImport(Import node) throws Exception {
			for (aliasType alias : node.names) {
				if (alias.asname != null
						&& ((NameTok) alias.asname).id.equals(name)) {
					newImportResolver().simulateImportAs(
							((NameTok) alias.name).id,
							((NameTok) alias.asname).id);
				} else if (((NameTok) alias.name).id.equals(name)) {
					newImportResolver().simulateImport(
							((NameTok) alias.name).id);
				}
			}

			return null;
		}

		private ImportTyper newImportResolver() {
			return new ImportTyper(model, enclosingScope, model
					.getTopLevel()) {

				@Override
				protected void put(Namespace scope, String name, Type type) {
					if (scope == null)
						throw new NullPointerException(
								"Need a namespace to bind name in");
					if (name == null)
						throw new NullPointerException("Need a name to bind");
					if (name.isEmpty())
						throw new IllegalArgumentException(
								"Name being bound must have at least one character");

					/*
					 * TODO: This way of doing it (following all the nested
					 * imports) suits the a priori symbol table approach but not
					 * a demand-driven approach.
					 * 
					 * Ideally the only import that ever occurs here should be
					 * the single import we asked to resolve, giving us a single
					 * type.
					 */
					if (scope.equals(enclosingScope)
							&& name.equals(BoundTypeGoal.this.name))
						boundTypes.add(type);
				}
			};
		}

		@Override
		public Object visitFunctionDef(FunctionDef node) throws Exception {
			if (judgement != null)
				return null;

			if (((NameTok) node.name).id.equals(name)) {
				Function function = enclosingScope.getFunctions().get(
						((NameTok) node.name).id);
				// If we can see the FunctionDef here, it _must_ already be
				// in the model.
				//
				// XXX: Not sure exactly how our model Functions and this
				// relate conceptually. We've had this issue before as
				// well. Needs more thought.
				assert function != null;
				boundTypes.add(new TFunction(function));
			}

			// Do NOT recurse into the FunctionDef body. Despite
			// appearances, it is not part of this namespace's code object.
			// It is a declaration of the nested function's code object.
			// Another way to think about it: the nested function's body is not
			// being 'executed' now whereas the enclosing namespace's
			// body is.

			return null;
		}

		@Override
		public Object visitFor(For node) throws Exception {
			if (judgement != null)
				return null;

			if (node.target instanceof Name
					&& ((Name) node.target).id.equals(name)) {
				// TODO: Try to infer type of iterable

				// Give up early because nothing beats Top
				judgement = new Top();
				return null;
			}

			// Either the body or the else block may rebind the loop
			// variable so we continue the search there
			for (stmtType stmt : node.body) {
				stmt.accept(this);
			}
			if (node.orelse != null)
				node.orelse.accept(this);

			return null;
		}

		@Override
		public Object visitClassDef(ClassDef node) throws Exception {
			if (judgement != null)
				return null;

			if (((NameTok) node.name).id.equals(name)) {
				Class klass = enclosingScope.getClasses().get(
						((NameTok) node.name).id);
				// If we can see the ClassDef here, it _must_ already be
				// in the model.
				//
				// XXX: Not sure exactly how our model Classes and this
				// relate conceptually. We've had this issue before as
				// well. Needs more thought.
				assert klass != null;
				boundTypes.add(new TClass(klass));
			}

			// Do NOT recurse into the ClassDef body. Despite
			// appearances, it is not part of this namespace's code object.
			// It is a declaration of the nested class's code object.
			// Another way to think about it: the class's body is not
			// being 'executed' now whereas the enclosing namespace's
			// body is.

			return null;
		}

		@Override
		public Object visitAssign(Assign node) throws Exception {
			if (judgement != null)
				return null;

			// TODO: compute rhs type on demand
			TypeJudgement rhsType = null;
			for (exprType lhsExpression : node.targets) {

				if (lhsExpression instanceof Name
						&& ((Name) lhsExpression).id.equals(name)) {
					// compute rhs type on demand
					if (rhsType == null) {

						rhsType = goalManager
								.registerSubgoal(new ExpressionTypeGoal(model,
										enclosingScope, node.value));
						// assert rhsType != null;
					}

					if (rhsType != null) {

						/*
						 * XXX: This is stupid. Shouldn't need this nasty
						 * interrogation. Can we get rid of the boundTypes set?
						 */
						if (rhsType instanceof SetBasedTypeJudgement) {
							boundTypes.addAll(((SetBasedTypeJudgement) rhsType)
									.getConstituentTypes());
						} else {

							judgement = new Top();
							return null;
						}
					}
				}
			}
			/*
			 * FIXME: If this search is happening on a global, we limit this
			 * search to the enclosing module scope completely ignoring the fact
			 * that another module can assign to the global by importing the
			 * module and referencing the variable explicitly. We're also
			 * ignoring the __builtin__ module.
			 */

			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			if (judgement == null) {
				// Traverse by default so that we catch all assignments even
				// if they are nested
				node.traverse(this);
			}
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((enclosingScope == null) ? 0 : enclosingScope.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BoundTypeGoal))
			return false;
		BoundTypeGoal other = (BoundTypeGoal) obj;
		if (enclosingScope == null) {
			if (other.enclosingScope != null)
				return false;
		} else if (!enclosingScope.equals(other.enclosingScope))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BoundTypeGoal [enclosingScope=" + enclosingScope + ", name="
				+ name + "]";
	}

}