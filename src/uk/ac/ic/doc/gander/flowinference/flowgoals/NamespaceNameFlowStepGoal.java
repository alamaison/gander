package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.WholeModelImportSimulation;
import uk.ac.ic.doc.gander.model.AttributeAccessFinder;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.ParentSiteFinder;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.codeobject.NamedCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NestedCodeObjects;
import uk.ac.ic.doc.gander.model.name_binding.NameScopeFinder;
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

	@Override
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
	 * <li>the namespace of any class inheriting from this namespace's code
	 * object unless that class defines a class or function of the same name</li>
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
	@Override
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
		assert goalManager != null;
		assert name != null;

		this.goalManager = goalManager;
		this.namespaceName = name;

		positions.add(new FiniteResult<FlowPosition>(nakedNameReferences()));
		if (positions.isFinished())
			return;

		positions.add(new ExplicitNameReferenceFlower().positions());
		if (positions.isFinished())
			return;

		positions.add(new InheritedNameReferenceFlower().positions());
		if (positions.isFinished())
			return;

		positions.add(new ImportedKeyReferenceFlower().positions());
		if (positions.isFinished())
			return;

		positions.add(new ConstructorFlower().positions());
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
		Set<ModelSite<Name>> lexicallyBoundVariables = new NameScopeFinder(
				namespaceName).getNameBindings();
		for (ModelSite<Name> variable : lexicallyBoundVariables) {
			positions.add(new ExpressionPosition(variable));
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
			Processor<ModelSite<exprType>> {

		private Result<FlowPosition> positions;

		ExplicitNameReferenceFlower() {

			/*
			 * The first task is to find where our namespace can flow to.
			 * 
			 * This is done by the namespace instance because namespaces don't
			 * follow the code object slavishly. For example, a class's
			 * namespace flows to the class body as well as its instances
			 * methods.
			 * 
			 * XXX: these will always be attribute references so why don't we
			 * get the attributes instead of the expression on the LHS?
			 */
			Result<ModelSite<exprType>> namespaceReferences = namespaceName
					.namespace().references(goalManager);

			namespaceReferences.actOnResult(this);
		}

		@Override
		public void processInfiniteResult() {
			positions = TopFp.INSTANCE;
		}

		@Override
		public void processFiniteResult(
				Set<ModelSite<exprType>> namespaceReferences) {

			Set<FlowPosition> newPositions = new HashSet<FlowPosition>();
			for (ModelSite<exprType> expression : namespaceReferences) {

				addExpressionIfAttributeLHSIsOurs(expression, newPositions);

			}

			positions = new FiniteResult<FlowPosition>(newPositions);
		}

		public Result<FlowPosition> positions() {
			return positions;
		}

	}

	final class InheritedNameReferenceFlower implements
			Processor<ModelSite<exprType>> {

		private Result<FlowPosition> positions;

		InheritedNameReferenceFlower() {

			/*
			 * The first task is to find where our namespace can flow to.
			 * 
			 * This is done by the namespace instance because namespaces don't
			 * follow the code object slavishly. For example, a class's
			 * namespace flows to the class body as well as its instances
			 * methods.
			 * 
			 * XXX: these will always be attribute references so why don't we
			 * get the attributes instead of the expression on the LHS?
			 */
			Result<ModelSite<exprType>> namespaceReferences = namespaceName
					.namespace().references(goalManager);

			namespaceReferences.actOnResult(this);
		}

		public Result<FlowPosition> positions() {
			return positions;
		}

		@Override
		public void processFiniteResult(Set<ModelSite<exprType>> result) {

			Set<FlowPosition> inheritedPositions = new HashSet<FlowPosition>();

			for (ModelSite<exprType> namespaceReference : result) {

				ModelSite<SimpleNode> parentNode = ParentSiteFinder
						.findParent(namespaceReference);
				if (parentNode.astNode() instanceof ClassDef) {

					ClassDef classAst = (ClassDef) parentNode.astNode();
					if (Arrays.asList(classAst.bases).contains(
							namespaceReference.astNode())) {

						/*
						 * The expression has flowed to the superclass list of a
						 * class definition. Now we have to find turn this
						 * ClassDef node into a ClassCO so that we can
						 */

						ClassCO inheritingClass = (ClassCO) parentNode
								.codeObject().nestedCodeObjects()
								.findCodeObjectMatchingAstNode(classAst);

						if (inheritingClass == null)
							throw new AssertionError(
									"ClassDef found with no code object");

						/*
						 * Technically, the namespace name from the superclass
						 * flows to the subclass unconditionally. However, in
						 * practice, anything declared in the subclass overrides
						 * an inherited item of the same name so the flow is
						 * conditional here.
						 * 
						 * Although relying on the declared names of nested code
						 * objects seems like a bit of a hack, I can't work out
						 * a way it could fail. It turns out that the code block
						 * of the inheriting class doesn't even have access to
						 * the namespace of the superclass while it executes so
						 * there isn't a race condition where it might use a
						 * name before overriding it.
						 * 
						 * Even if the name doesn't appear in the superclass
						 * until after the elaboration phase, the declaration in
						 * the subclass will still override it.
						 */

						NestedCodeObjects overridingDeclarations = inheritingClass
								.nestedCodeObjects()
								.namedCodeObjectsDeclaredAs(
										namespaceName.name());

						if (overridingDeclarations.isEmpty()) {

							/*
							 * Inheritance doesn't really add names to the
							 * subclass's namespace in a qualified or
							 * unqualified way. Really the namespace delegates
							 * to the superclass when the lookup fails. That
							 * isn't modelled very well here
							 */
							assert inheritingClass.unqualifiedNamespace()
									.equals(inheritingClass
											.fullyQualifiedNamespace());
							inheritedPositions.add(new NamespaceNamePosition(
									new NamespaceName(namespaceName.name(),
											inheritingClass
													.unqualifiedNamespace())));
						}
					}

				}

			}

			positions = new FiniteResult<FlowPosition>(inheritedPositions);
		}

		@Override
		public void processInfiniteResult() {
			positions = TopFp.INSTANCE;
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
							node.traverse(this);
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
			positions.add(new ExpressionPosition(new ModelSite<Attribute>(
					attribute, enclosingCodeObject)));
		}
	}

	/**
	 * Add positions for flow of the namespace key's value caused by importing
	 * either the key itself or a code object allowing access to the namespace
	 * containing it.
	 * 
	 * Our namespace's key could be imported anywhere in the system so we have
	 * to walk the entire thing searching any code block for import statements
	 * that result in our key's value being bound to a new key.
	 */
	final class ImportedKeyReferenceFlower {

		/*
		 * Can never be Top because we believe there is no such thing as an
		 * import we can't follow.
		 */
		private final RedundancyEliminator<FlowPosition> importedReferences = new RedundancyEliminator<FlowPosition>();

		private final Binder<NamespaceName, Namespace, CodeObject, ModuleCO> worker = new Binder<NamespaceName, Namespace, CodeObject, ModuleCO>() {

			@Override
			public void bindModuleToLocalName(ModuleCO loadedModule,
					String name, CodeObject container) {
				/*
				 * The binding occurs as though it were being bound to a
				 * variable in a particular code object, the code object
				 * containing the import statement. Like with all variables,
				 * this may not be the code object whose unqualified namespace
				 * the variable binds in.
				 */
				if (!importedReferences.isFinished()) {
					handleBind(loadedModule, new Variable(name, container));
				}
			}

			@Override
			public void bindModuleToName(ModuleCO loadedModule, String name,
					ModuleCO receivingModule) {
				/*
				 * The binding occurs as though it were being bound to a global
				 * variable in a particular module.
				 */
				if (!importedReferences.isFinished()) {
					handleBind(loadedModule,
							new Variable(name, receivingModule));
				}
			}

			@Override
			public void bindObjectToLocalName(NamespaceName importedObject,
					String name, CodeObject container) {
				/*
				 * The binding occurs as though it were being bound to a
				 * variable in a particular code object, the code object
				 * containing the import statement. Like with all variables,
				 * this may not be the code object whose unqualified namespace
				 * the variable binds in.
				 */
				if (!importedReferences.isFinished()) {
					handleBind(importedObject, new Variable(name, container));
				}
			}

			@Override
			public void bindObjectToName(NamespaceName importedObject,
					String name, ModuleCO receivingModule) {
				/*
				 * The binding occurs as though it were being bound to a global
				 * variable in a particular module.
				 */
				if (!importedReferences.isFinished()) {
					handleBind(importedObject, new Variable(name,
							receivingModule));
				}
			}

			@Override
			public void onUnresolvedImport(
					Import<CodeObject, ModuleCO> importInstance, String name,
					ModuleCO receivingModule) {
				/*
				 * WARNING: We pretend that unresolved imports don't matter
				 * because they would swamp our results with Top. All flow
				 * results would return Top if even a single import were
				 * unresolved.
				 */
			}

			@Override
			public void onUnresolvedLocalImport(
					Import<CodeObject, ModuleCO> importInstance, String name) {
				/*
				 * WARNING: We pretend that unresolved imports don't matter
				 * because they would swamp our results with Top. All flow
				 * results would return Top if even a single import were
				 * unresolved.
				 */
			}

			@Override
			public void bindAllNamespaceMembers(Namespace sourceNamespace,
					CodeObject container) {
				if (!importedReferences.isFinished()) {
					handleBindAll(sourceNamespace, container);
				}
			}
		};

		ImportedKeyReferenceFlower() {

			new WholeModelImportSimulation(namespaceName.namespace().model(),
					worker);
		}

		Result<FlowPosition> positions() {
			return importedReferences.result();
		}

		/**
		 * Handle an import that may cause our namespace name to flow elsewhere.
		 * 
		 * When this method is called, an import has caused an object to be
		 * bound to a variable somewhere. There are two ways that this can flow
		 * our namespace name onwards. Firstly, the imported object my be an
		 * alias of our namespace name so it flows to the variable the object is
		 * bound to. Secondly, the imported object may give access to our
		 * namespace name through member access. In this situation, our
		 * namespace name flows to all expressions where the imported object is
		 * the subject of an attribute access.
		 * 
		 * @param object
		 *            the object being bound by an import
		 * @param objectBinding
		 *            variableName the variable it is being bound to
		 */
		private void handleBind(CodeObject object, Variable objectBinding) {
			assert variableBindsLocallyOrGlobally(objectBinding);

			if (importedObjectAliasesOurName(object)) {

				/*
				 * The imported object directly aliases the namespace name we
				 * care about.
				 */
				/* import key */
				/* from module import key */
				importedReferences.add(variableFlowPositions(objectBinding));

			} else if (codeObjectAllowsAttributesToAccessNamespace(object,
					namespaceName.namespace())) {

				/*
				 * The imported object potentially allows access to the
				 * namespace name we care about by way of an attribute access.
				 */
				/*
				 * import module
				 * 
				 * module.key
				 */
				/*
				 * from module import object
				 * 
				 * object.key
				 */
				importedReferences
						.add(referencesToAttributeOfImportedCodeObject(objectBinding));
			}
		}

		void handleBind(NamespaceName object, Variable objectBinding) {
			assert variableBindsLocallyOrGlobally(objectBinding);

			if (object.equals(namespaceName)) {

				/*
				 * The imported object directly aliases the namespace name we
				 * care about.
				 */
				/* import key */
				/* from module import key */
				importedReferences.add(variableFlowPositions(objectBinding));

			} else {
				/*
				 * A namespace name (not ours) was imported into this namespace
				 * and bound to a name which may permit access to our namespace
				 * name by attribute access on the new name. Check if we are
				 * able to flow to this other namespace. If so, check whether we
				 * do, indeed, access that alias via attribute access on the
				 * imported name because anywhere that happens may lead to our
				 * namespace name flowing onwards.
				 */

				// FIXME: HOW?!
				// if (weCanFlowToNameInImportedObjectsNamespace) {

				/*
				 * The imported object potentially allows access to the
				 * namespace name we care about by way of an attribute access.
				 */
				/*
				 * import module
				 * 
				 * module.key
				 */
				/*
				 * from module import object
				 * 
				 * object.key
				 */
				// importedReferences
				// .add(referencesToAttributeOfImportedCodeObject(objectBinding));
				// }
				// }
			}
		}

		void handleBindAll(Namespace sourceNamespace, CodeObject container) {
			// starred-import only allowed at top level
			assert container instanceof ModuleCO;

			if (namespaceName.namespace().equals(sourceNamespace)) {

				/*
				 * The imported object directly aliases the namespace name we
				 * care about.
				 */
				/* from module import * (where key is in *) */
				importedReferences.add(variableFlowPositions(new Variable(
						namespaceName.name(), container)));

			} else {
				/*
				 * A namespace name (not ours) was imported into this namespace
				 * and bound to a name which may permit access to our namespace
				 * name by attribute access on the new name. Check if we are
				 * able to flow to this other namespace. If so, check whether we
				 * do, indeed, access that alias via attribute access on the
				 * imported name because anywhere that happens may lead to our
				 * namespace name flowing onwards.
				 */

				// FIXME: HOW?!
				// if (weCanFlowToNameInImportedObjectsNamespace) {

				/*
				 * The imported object potentially allows access to the
				 * namespace name we care about by way of an attribute access.
				 */
				/*
				 * import module
				 * 
				 * module.key
				 */
				/*
				 * from module import object
				 * 
				 * object.key
				 */
				// importedReferences
				// .add(referencesToAttributeOfImportedCodeObject(objectBinding));
				// }
				// }
			}
		}

		private boolean importedObjectAliasesOurName(CodeObject object) {

			/*
			 * XXX: HACK: comparing the name by name of code object is BAD. What
			 * if the code object was aliased and that alias was imported?
			 * Should fix simulator so it tells us the actual key that was
			 * imported from the other namespace.
			 * 
			 * XXX: HACK: using the loadedObject's parent to check if the object
			 * came from our namespace is BAD. What if the thing was imported
			 * from somewhere else then imported again? Should fix simulator so
			 * it tells us where the key was actually imported from.
			 */

			return object instanceof NestedCodeObject
					&& ((NestedCodeObject) object).parent()
							.unqualifiedNamespace()
							.equals(namespaceName.namespace())
					&& object instanceof NamedCodeObject
					&& ((NamedCodeObject) object).declaredName().equals(
							namespaceName.name());
		}

		private boolean codeObjectAllowsAttributesToAccessNamespace(
				CodeObject loadedObject, Namespace namespace) {

			return loadedObject.fullyQualifiedNamespace().equals(namespace);
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
			Variable objectBinding) {

		/*
		 * The value of our namespace's code object may flow all over the place
		 * and be subject to attribute access at any of these locations so we
		 * issue a new flow query to track our namespace rather than its key.
		 */

		Result<ModelSite<exprType>> moduleReferences = goalManager
				.registerSubgoal(new FlowGoal(new NamespaceNamePosition(
						new NamespaceName(objectBinding.bindingLocation()))));

		Transformer<ModelSite<exprType>, Result<FlowPosition>> accessPositioner = new Transformer<ModelSite<exprType>, Result<FlowPosition>>() {

			@Override
			public Result<FlowPosition> transformFiniteResult(
					Set<ModelSite<exprType>> moduleReferences) {
				return new FiniteResult<FlowPosition>(
						findAccessesToCodeObjectNamespaceName(moduleReferences));
			}

			@Override
			public Result<FlowPosition> transformInfiniteResult() {
				return TopFp.INSTANCE;
			}
		};

		return moduleReferences.transformResult(accessPositioner);
	}

	/**
	 * Search for any accesses the the given attribute name on the expressions
	 * the code object has flowed to.
	 */
	private Set<FlowPosition> findAccessesToCodeObjectNamespaceName(
			Set<ModelSite<exprType>> moduleReferenceExpressions) {

		final Set<FlowPosition> positions = new HashSet<FlowPosition>();

		new AttributeAccessFinder(moduleReferenceExpressions,
				new AttributeAccessFinder.Event() {

					@Override
					public boolean attributeAccess(
							ModelSite<Attribute> attribute) {
						if (((NameTok) attribute.astNode().attr).id
								.equals(namespaceName.name())) {
							positions.add(new ExpressionPosition(attribute));
						}
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
	private Result<FlowPosition> variableFlowPositions(Variable importAs) {
		assert namespaceName.namespace() instanceof Module;

		/*
		 * importReceiver is the code object containing the import statement but
		 * its namespace may not actually be the namespace that the new key is
		 * added to. It depends on the binding scope of 'as' in importReceiver.
		 * It could be the global scope so we resolve the name here.
		 */

		return new FiniteResult<FlowPosition>(
				Collections.singleton(new NamespaceNamePosition(
						new NamespaceName(importAs.bindingLocation()))));
	}

	/**
	 * The value of an __init__ member of a class code code object can be
	 * considered as 'flowing to' any positions of the class object that are
	 * subject to a call.
	 */
	private final class ConstructorFlower implements
			Processor<ModelSite<exprType>> {

		private Result<FlowPosition> positions;

		ConstructorFlower() {

			if (namespaceName.namespace() instanceof Class
					&& namespaceName.name().equals("__init__")) {

				Class classObject = (Class) namespaceName.namespace();
				Result<ModelSite<exprType>> classObjectPositions = goalManager
						.registerSubgoal(new FlowGoal(
								new CodeObjectDefinitionPosition(classObject
										.codeObject())));

				classObjectPositions.actOnResult(this);

			} else {
				positions = FiniteResult.bottom();
			}
		}

		@Override
		public void processInfiniteResult() {
			positions = TopFp.INSTANCE;
		}

		@Override
		public void processFiniteResult(
				Set<ModelSite<exprType>> classObjectPositions) {

			Set<FlowPosition> newFlowPositions = new HashSet<FlowPosition>();

			/*
			 * The code object associated with the __init__ member only flows to
			 * where the code object is subject to a call.
			 */
			for (ModelSite<exprType> expression : classObjectPositions) {

				SimpleNode parentNode = AstParentNodeFinder.findParent(
						expression.astNode(), expression.codeObject().ast());

				if (parentNode instanceof Call) {
					newFlowPositions.add(new ExpressionPosition(
							new ModelSite<exprType>(expression.astNode(),
									expression.codeObject())));
				}
			}

			positions = new FiniteResult<FlowPosition>(newFlowPositions);
		}

		Result<FlowPosition> positions() {
			return positions;
		}

	}

	public boolean variableBindsLocallyOrGlobally(Variable variable) {
		return variable.bindingLocation().codeObject()
				.equals(variable.codeObject())
				|| variable.bindingLocation().codeObject()
						.equals(variable.codeObject().enclosingModule());
	}
}
