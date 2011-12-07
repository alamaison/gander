package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.modelgoals.NameScopeGoal;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.importing.DefaultImportSimulator;
import uk.ac.ic.doc.gander.importing.WholeModelImportSimulation;
import uk.ac.ic.doc.gander.importing.DefaultImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.AttributeAccessFinder;
import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.codeobject.NamedCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Goal modelling the flow of a value out of a namespace over one execution
 * step.
 * 
 * Values in a namespace are associated with a key which is just a string. There
 * are four ways for this key's value to flow out of the namespace.
 * 
 * Firstly, {@link Name}s in the scope of this namespace's code object are able
 * to reference keys directly by key name alone.
 * 
 * <pre>
 * x = 4     # sets key 'x' to value 4 in this namespace 
 * print x   # refers to the same key 'x' without explicitly stating namespace
 * </pre>
 * 
 * Secondly, this namespace's code object's value can flow within the module in
 * which it is defined (subject to the rules of binding and lexical scoping). It
 * may flow to places that are then subject to attribute access which, for class
 * objects, allows the class namespace's key value to flow out.
 * 
 * <pre>
 * class X:
 *     a = "bob"    # sets key 'a' to value "bob" in X's namespace 
 * 
 * print X.bob      # refers to X's key 'a' explicitly stating namespace
 * </pre>
 * 
 * Thirdly, the value reference by a key in one namespace can be added to
 * another namespace either with the same or a different key by importing the
 * key from the namespace.
 * 
 * In module a:
 * 
 * <pre>
 * G = 42
 * </pre>
 * 
 * In module b:
 * 
 * <pre>
 * from a import G
 * print G    # 42 flows here
 * </pre>
 * 
 * Fourthly, a module code object can be imported in another code object which
 * binds the module to a key in the second code object's namespace. That key can
 * be subject to attribute access causing the value of the named key to flow out
 * of it.
 * 
 * In module a:
 * 
 * <pre>
 * G = 42
 * </pre>
 * 
 * In module b:
 * 
 * <pre>
 * import a
 * print a.G    # 42 flows here
 * </pre>
 */
final class NamespaceNameFlowStepGoal implements FlowStepGoal {

	private final NamespaceName name;

	public NamespaceNameFlowStepGoal(NamespaceName name) {
		this.name = name;
	}

	public Result<FlowPosition> initialSolution() {
		return FiniteResult.bottom();
	}

	/**
	 * A function object, class object or regular flows in a single step to:
	 * <ul>
	 * <li>the namespace of the code block in which it is defined as the name
	 * with which it is declared</li>
	 * <li>the namespace of any code block containing {@code from l import X}
	 * with the name {@code X}</li>
	 * </ul>
	 * 
	 * A module's flows is more complicated. A module {@code m} flows in a
	 * single step to (assuming always that {@code m} resolves to the module in
	 * question taking relative paths into account):
	 * <ul>
	 * <li>the namespace of any code block containing {@code import m} with the
	 * name {@code m}</li>
	 * <li>the namespace of any code block containing {@code import m as p} with
	 * the name {@code p}</li>
	 * <li>the namespace of any module that appears before {@code m} in a string
	 * such as {@code import k.l.m}, in this case {@code l}, with the name
	 * {@code m}</li>
	 * <li>the namespace of any module that appears before {@code m} in a string
	 * such as {@code import k.l.m as p}, in this case {@code l}, with the name
	 * {@code m} and into the namespace of the code block containing the import
	 * statement with the name {@code p}</li>
	 * <li>the namespace of any code block containing {@code from l import m}
	 * with the name {@code m}</li>
	 * </ul>
	 * */
	public Result<FlowPosition> recalculateSolution(SubgoalManager goalManager) {
		return new NamespaceNameFlowStepGoalSolver(goalManager, name)
				.solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NamespaceNameFlowStepGoal other = (NamespaceNameFlowStepGoal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceNameFlowStepGoal [name=" + name + "]";
	}

}

final class NamespaceNameFlowStepGoalSolver {

	private final SubgoalManager goalManager;
	private final RedundancyEliminator<FlowPosition> positions = new RedundancyEliminator<FlowPosition>();
	private final NamespaceName namespaceName;

	public NamespaceNameFlowStepGoalSolver(SubgoalManager goalManager,
			NamespaceName name) {
		this.goalManager = goalManager;
		this.namespaceName = name;

		positions.add(new FiniteResult<FlowPosition>(nakedNameReferences()));
		if (positions.isFinished())
			return;

		positions.add(new ExplicitNameReferenceFlower().positions());
		if (positions.isFinished())
			return;

		positions.add(new ImportedKeyReferenceFlower().importedReferences);
		if (positions.isFinished())
			return;
	}

	public Result<FlowPosition> solution() {
		return positions.result();
	}

	/**
	 * Flow positions for flow of the namespace key's value to 'naked' name
	 * references.
	 * 
	 * These are the names in the namespace's code object that bind that name in
	 * that namespace. In other words, the names in that namespace's code object
	 * that aren't shadowed by a local variable or global keyword.
	 */
	private Set<FlowPosition> nakedNameReferences() {
		final Set<FlowPosition> positions = new HashSet<FlowPosition>();

		/*
		 * The name in the given namespace could be referenced by a 'naked' name
		 * throughout the nested code blocks so we walk through them and check
		 * if the name use in that code block binds in the given namespace. If
		 * it does, this name in the namespace flows to all uses of that name in
		 * the codeblock.
		 */
		Set<ModelSite<Name>> lexicallyBoundVariables = goalManager
				.registerSubgoal(new NameScopeGoal(namespaceName));
		for (ModelSite<Name> variable : lexicallyBoundVariables) {
			positions.add(new ExpressionPosition<Name>(variable));
		}

		return positions;
	}

	/**
	 * Flow positions for the flow of name's value to references where the
	 * namespace is explicitly specified.
	 * 
	 * These are attribute expressions anywhere in the system where the
	 * left-hand part of the attribute can refer to the namespace's code object,
	 * the right-hand part is the name in question and the code object supports
	 * namespace name reference through attribute access on the code object. The
	 * code objects that support this are modules and classes. Specifically,
	 * functions do not allow they namespaces to be accessed this way as that
	 * would allow local variables to be changed from outside the function body.
	 */
	final class ExplicitNameReferenceFlower implements
			Processor<ModelSite<? extends exprType>> {

		private Result<FlowPosition> positions;

		ExplicitNameReferenceFlower() {

			/*
			 * The first task is to find where our namespace can flow to.
			 * 
			 * This is done using CodeObjectNamespacePosition because namespaces
			 * don't follow the code object slavishly. For example, a class's
			 * namespace flows to the class body as well as its instances
			 * methods.
			 * 
			 * XXX: these will always be attribute references so why don't we
			 * get the attributes instead of the expression on the LHS?
			 */
			Result<ModelSite<? extends exprType>> namespaceReferences = goalManager
					.registerSubgoal(new FlowGoal(
							new CodeObjectNamespacePosition(namespaceName
									.namespace().codeObject())));

			namespaceReferences.actOnResult(this);
		}

		public void processInfiniteResult() {
			positions = TopFp.INSTANCE;
		}

		public void processFiniteResult(
				Set<ModelSite<? extends exprType>> namespaceReferences) {

			Set<FlowPosition> newPositions = new HashSet<FlowPosition>();
			for (ModelSite<? extends exprType> expression : namespaceReferences) {

				addExpressionIfAttributeLHSIsOurs(expression, newPositions);

			}

			positions = new FiniteResult<FlowPosition>(newPositions);
		}

		public Result<FlowPosition> positions() {
			return positions;
		}

	}

	private void addExpressionIfAttributeLHSIsOurs(
			final ModelSite<?> codeObjectReference,
			final Set<FlowPosition> positions) {

		new CodeObjectWalker() {

			@Override
			protected void visitCodeObject(final CodeObject codeObject) {
				try {
					codeObject.codeBlock().accept(new LocalCodeBlockVisitor() {

						@Override
						public Object visitAttribute(Attribute node)
								throws Exception {
							addPositionIfAttributeMatches(codeObjectReference,
									positions, codeObject, node);
							return null;
						}

						@Override
						protected Object unhandled_node(SimpleNode node)
								throws Exception {
							return null;
						}

						@Override
						public void traverse(SimpleNode node) throws Exception {
							node.traverse(this);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}.walk(codeObjectReference.codeObject());
	}

	private void addPositionIfAttributeMatches(
			ModelSite<?> codeObjectReference, Set<FlowPosition> positions,
			CodeObject enclosingCodeObject, Attribute attribute) {

		if (attribute.value.equals(codeObjectReference.astNode())) {

			addPositionIfAttributeNameMatches(positions, enclosingCodeObject,
					attribute, namespaceName.name());

		}
	}

	private static void addPositionIfAttributeNameMatches(
			Set<FlowPosition> positions, CodeObject enclosingCodeObject,
			Attribute attribute, String name) {
		if (((NameTok) attribute.attr).id.equals(name)) {
			positions.add(new ExpressionPosition<Attribute>(
					new ModelSite<Attribute>(attribute, enclosingCodeObject)));
		}
	}

	/**
	 * Add positions for flow of the namespace key's value caused by importing
	 * either the key itself or the code object of the namespace containing it.
	 */
	final class ImportedKeyReferenceFlower {

		/*
		 * Can never be Top because we believe there is no such thing as an
		 * import we can't follow.
		 */
		Result<FlowPosition> importedReferences = FiniteResult.bottom();

		ImportedKeyReferenceFlower() {

			/*
			 * Our namespace's key could be imported anywhere in the system so
			 * we have to walk the entire thing searching any code block for
			 * import statements that result in our key's value bound to a new
			 * key.
			 * 
			 * That somewhere is not necessarily the namespace of the code
			 * object containing the import statement. If the name it is bound
			 * to is declared global then it is bound to a key in the global
			 * namespace instead.
			 */

			Binder<CodeObject, CodeObject, ModuleCO> worker = new DefaultImportSimulator.Binder<CodeObject, CodeObject, ModuleCO>() {

				public void bindName(CodeObject loadedObject, String name,
						CodeObject importReceiver) {
					/*
					 * XXX: HACK: comparing the name by name of code object is
					 * BAD. What if the code object was aliased and that alias
					 * was imported? Should fix simulator so it tells us the
					 * actual key that was imported from the other namespace.
					 * 
					 * XXX: HACK: using the loadedObject's parent to check if
					 * the object came from our namespace is BAD. What if the
					 * thing was imported from somewhere else then imported
					 * again? Should fix simulator so it tells us where the key
					 * was actually imported from.
					 */

					if (codeObjectAllowsAttributesToAccessNamespace(
							loadedObject, namespaceName.namespace())) {

						/* import codeobject */

						assert loadedObject instanceof ModuleCO;
						assert namespaceName.namespace() instanceof Module;

						/*
						 * importReceiver is the code object containing the
						 * import statement but its namespace may not actually
						 * be where the loaded object is bound to a name. It
						 * depends on the binding scope of 'name' in
						 * importReceiver. It could be the global scope so we
						 * resolve the name here.
						 */
						final NamespaceName loadedCodeObjectBinding = new Variable(
								name, importReceiver).bindingLocation();
						assert nameBindsLocallyOrGlobally(importReceiver,
								loadedCodeObjectBinding);

						importedReferences = referencesToAttributeOfImportedCodeObject(
								loadedCodeObjectBinding, name);

					} else if (loadedObject instanceof NestedCodeObject) {
						if (importReceiver.model().intrinsicNamespace(
								((NestedCodeObject) loadedObject).parent())
								.equals(namespaceName.namespace())) {
							if (loadedObject instanceof NamedCodeObject) {
								if (((NamedCodeObject) loadedObject)
										.declaredName().equals(
												namespaceName.name())) {
									/* from codeobject import key */
									importedReferences = new FiniteResult<FlowPosition>(
											referencesToImportedKey(
													importReceiver, name));
								}
							}
						}
					}

				}

				private boolean codeObjectAllowsAttributesToAccessNamespace(
						CodeObject loadedObject, Namespace namespace) {

					return loadedObject.model()
							.intrinsicNamespace(loadedObject).equals(namespace);
				}

				public void onUnresolvedImport(List<String> importPath,
						ModuleCO relativeTo, String as, CodeObject codeBlock) {
					// TODO Auto-generated method stub

				}

				public void onUnresolvedImportFromItem(List<String> fromPath,
						ModuleCO relativeTo, String itemName, String as,
						CodeObject codeBlock) {
					// TODO Auto-generated method stub

				}

			};
			new WholeModelImportSimulation(namespaceName.namespace().model(),
					worker);

		}
	}

	/**
	 * Add flow positions for places where a value flows out of our namespace as
	 * a result of an 'import module' style import.
	 * 
	 * This means our code object (which will always be a module in this case)
	 * was imported into another code object. Now we need to see if our code
	 * object's namespace is accessed. For code objects that permit it (classes
	 * and modules) this happens through attribute expressions on the code
	 * object where the attribute name is the name of the key being accessed.
	 */
	private Result<FlowPosition> referencesToAttributeOfImportedCodeObject(
			NamespaceName loadedModuleBinding, String name) {

		if (!loadedModuleBinding.equals(namespaceName)) {

			/*
			 * The value of our namespace's code object may flow all over the
			 * place and be subject to attribute access at any of these
			 * locations so we issue a new flow query to track our namespace
			 * rather than its key.
			 */

			Result<ModelSite<? extends exprType>> moduleReferences = goalManager
					.registerSubgoal(new FlowGoal(new NamespaceNamePosition(
							loadedModuleBinding)));

			Transformer<ModelSite<? extends exprType>, Result<FlowPosition>> accessPositioner = new Transformer<ModelSite<? extends exprType>, Result<FlowPosition>>() {

				public Result<FlowPosition> transformFiniteResult(
						Set<ModelSite<? extends exprType>> moduleReferences) {
					return new FiniteResult<FlowPosition>(
							findNameAccessesToCodeObjectNamespace(moduleReferences));
				}

				public Result<FlowPosition> transformInfiniteResult() {
					return TopFp.INSTANCE;
				}
			};

			return moduleReferences.transformResult(accessPositioner);

		} else {
			return FiniteResult.bottom();
		}
	}

	/**
	 * Search for any accesses the the given attribute name on the expressions
	 * the code object has flowed to.
	 */
	private Set<FlowPosition> findNameAccessesToCodeObjectNamespace(
			Set<ModelSite<? extends exprType>> moduleReferenceExpressions) {

		final Set<FlowPosition> positions = new HashSet<FlowPosition>();

		new AttributeAccessFinder(moduleReferenceExpressions,
				new AttributeAccessFinder.Event() {

					public boolean attributeAccess(
							ModelSite<Attribute> attribute) {
						positions.add(new ExpressionPosition<Attribute>(
								attribute));
						return false;
					}
				});
		return positions;
	}

	/**
	 * Add flow positions for places where a value flows out of our namespace as
	 * a result of a 'from module import key' style import.
	 * 
	 * This means our key's was imported from our code object (which will always
	 * be a module in this case) into another code object's namespace,
	 * optionally with a different key name. Now we need to see where this new
	 * key is used.
	 * 
	 * This new key's value does not track the old key's value---in other words,
	 * if the old namespace changes the value associated with that key, the new
	 * key's value does not change as well---however, as static analysis can't
	 * determine exactly what value the old key has when the import occurs, the
	 * analysis must flow all values arriving at the old key to the new key.
	 */
	protected Set<FlowPosition> referencesToImportedKey(
			CodeObject importReceiver, String as) {
		assert namespaceName.namespace() instanceof Module;

		/*
		 * importReceiver is the code object containing the import statement but
		 * its namespace may not actually be the namespace that the new key is
		 * added to. It depends on the binding scope of 'as' in importReceiver.
		 * It could be the global scope so we resolve the name here.
		 */
		Variable importAs = new Variable(as, importReceiver);
		assert nameBindsLocallyOrGlobally(importReceiver, importAs
				.bindingLocation());

		return Collections.<FlowPosition> singleton(new NamespaceNamePosition(
				importAs.bindingLocation()));
	}

	private boolean nameBindsLocallyOrGlobally(CodeObject importReceiver,
			NamespaceName nameBinding) {
		Model model = importReceiver.model();

		return nameBinding.namespace().equals(
				model.intrinsicNamespace(importReceiver))
				|| nameBinding.namespace().equals(
						model.intrinsicNamespace(importReceiver
								.enclosingModule()));
	}
}
