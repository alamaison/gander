package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.modelgoals.BoundNamesGoal;
import uk.ac.ic.doc.gander.importing.ImportSimulationWatcher;
import uk.ac.ic.doc.gander.importing.WholeModelImportSimulation;
import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.name_binding.Binder;
import uk.ac.ic.doc.gander.model.name_binding.NamespaceKey;

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
final class NamespaceKeyFlowStepGoal implements FlowStepGoal {

	private final NamespaceKey namespaceKey;

	NamespaceKeyFlowStepGoal(NamespaceKey namespaceKey) {
		this.namespaceKey = namespaceKey;
	}

	public Set<FlowPosition> initialSolution() {
		return Collections.emptySet();
	}

	public Set<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		Set<FlowPosition> positions = new HashSet<FlowPosition>();

		addNakedNameReferences(positions, goalManager);
		addExplicitNameReferences(positions, goalManager);
		addImportedKeyReferences(positions, goalManager);

		return positions;
	}

	/**
	 * Add positions for flow of the namespace key's value to 'naked' name
	 * references.
	 * 
	 * These are the names in the namespace's code object that bind that name in
	 * that namespace. In other words, the names in that namespace's code object
	 * that aren't shadowed by a local variable or global keyword.
	 */
	private void addNakedNameReferences(final Set<FlowPosition> positions,
			SubgoalManager goalManager) {
		/*
		 * The name in the given namespace could be referenced by a 'naked' name
		 * throughout the nested code blocks so we walk through them and check
		 * if the name use in that code block binds in the given namespace. If
		 * it does, this name in the namespace flows to all uses of that name in
		 * the codeblock.
		 */
		Set<ModelSite<Name>> lexicallyBoundNames = goalManager
				.registerSubgoal(new BoundNamesGoal(namespaceKey));
		for (ModelSite<Name> nameSite : lexicallyBoundNames) {
			positions.add(new ExpressionPosition<Name>(nameSite));
		}
	}

	/**
	 * Add positions for the flow of name's value to references where the
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
	private void addExplicitNameReferences(final Set<FlowPosition> positions,
			SubgoalManager goalManager) {

		/*
		 * The name can flow beyond this namespace's code block if it is
		 * imported anywhere. This can happen in two ways. The namespace itself
		 * is imported into the other namespace and the name is referenced by
		 * attribute or the particular name is imported into the other
		 * namespace.
		 * 
		 * The first task is to find any namespace that imports this namespace's
		 * code object then search for attributes that reference the name that
		 * the code object was bound to. This handles the first case
		 */
		Set<ModelSite<? extends exprType>> moduleReferences = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(
						namespaceKey.getNamespace(), namespaceKey.getModel())));
		for (ModelSite<? extends exprType> expression : moduleReferences) {

			addExpressionIfAttributeLHSIsOurs(expression, positions);

		}
	}

	/**
	 * Add positions for flow of the namespace key's value caused by importing
	 * either the key itself or the code object of the namespace containing it.
	 */
	private void addImportedKeyReferences(final Set<FlowPosition> positions,
			final SubgoalManager goalManager) {

		/*
		 * Our namespace's key could be imported anywhere in the system so we
		 * have to walk the entire thing searching any code block for import
		 * statements that result in our key's value bound to a new key.
		 * 
		 * That somewhere is not necessarily the namespace of the code object
		 * containing the import statement. If the name it is bound to is
		 * declared global then it is bound to a key in the global namespace
		 * instead.
		 */

		ImportSimulationWatcher worker = new ImportSimulationWatcher() {

			public void bindingName(Namespace importReceiver,
					Namespace loadedObject, String as) {

				/*
				 * XXX: HACK: comparing the name by name of code object is BAD.
				 * What if the code object was aliased and that alias was
				 * imported? Should fix simulator so it tells us the actual key
				 * that was imported from the other namespace.
				 * 
				 * XXX: HACK: using the loadedObject's parent to check if the
				 * object came from our namespace is BAD. What if the thing was
				 * imported from somewhere else then imported again? Should fix
				 * simulator so it tells us where the key was actually imported
				 * from.
				 */
				if (loadedObject.getParentScope().equals(
						namespaceKey.getNamespace())
						&& loadedObject.getName()
								.equals(namespaceKey.getName())) {
					/* from codeobject import key */
					addReferencesToImportedKey(importReceiver, loadedObject,
							as, positions, goalManager);
				} else if (loadedObject.equals(namespaceKey.getNamespace())) {

					/* import codeobject */
					addReferencesToKeyOfImportedCodeObject(importReceiver,
							loadedObject, as, positions, goalManager);
				}

			}

		};
		new WholeModelImportSimulation(namespaceKey.getModel(), worker);

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
	protected void addReferencesToKeyOfImportedCodeObject(
			Namespace importReceiver, Namespace loadedObject, String as,
			Set<FlowPosition> positions, SubgoalManager goalManager) {
		assert namespaceKey.getNamespace() instanceof Module;
		assert loadedObject.equals(namespaceKey.getNamespace());

		/*
		 * importReceiver is the code object containing the import statement but
		 * its namespace may not actually be the import receiver. It depends on
		 * the binding scope of 'as' in importReceiver. It could be the global
		 * scope so we resolve the name here.
		 */
		NamespaceKey importedCodeObjectKey = Binder.resolveBindingScope(as,
				importReceiver, namespaceKey.getModel());
		assert importedCodeObjectKey.getNamespace().equals(importReceiver)
				|| importedCodeObjectKey.getNamespace().equals(
						importReceiver.getGlobalNamespace());

		/*
		 * The value of our namespace's code object may flow all over the place
		 * and be subject to attribute access at any of these locations so we
		 * issue a new flow query to track our namespace rather than its key.
		 */
		if (!importedCodeObjectKey.equals(namespaceKey)) {

			// Set<ModelSite<? extends exprType>> moduleReferences = goalManager
			// .registerSubgoal(new FlowGoal(new CodeObjectPosition(
			// namespaceKey.getNamespace(), namespaceKey)));

			Set<ModelSite<? extends exprType>> moduleReferences = goalManager
					.registerSubgoal(new FlowGoal(new NamespaceKeyPosition(
							importedCodeObjectKey)));

			addAccessesToNamespaceEntry(moduleReferences, positions);
		}
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
	protected void addReferencesToImportedKey(Namespace importReceiver,
			Namespace loadedObject, String as, Set<FlowPosition> positions,
			SubgoalManager goalManager) {
		assert namespaceKey.getNamespace() instanceof Module;

		/*
		 * importReceiver is the code object containing the import statement but
		 * its namespace may not actually be the namespace that the new key is
		 * added to. It depends on the binding scope of 'as' in importReceiver.
		 * It could be the global scope so we resolve the name here.
		 */
		NamespaceKey newKey = Binder.resolveBindingScope(as, importReceiver,
				namespaceKey.getModel());
		assert newKey.getNamespace().equals(importReceiver)
				|| newKey.getNamespace().equals(
						importReceiver.getGlobalNamespace());
		positions.add(new NamespaceKeyPosition(newKey));
	}

	private void addExpressionIfAttributeLHSIsOurs(
			final ModelSite<?> codeObjectReferenceSite,
			final Set<FlowPosition> positions) {

		new CodeObjectWalker() {

			@Override
			protected void visitCodeObject(final Namespace codeBlock) {

				try {
					codeBlock.asCodeBlock().accept(new LocalCodeBlockVisitor() {

						@Override
						public Object visitAttribute(Attribute node)
								throws Exception {
							if (node.value.equals(codeObjectReferenceSite
									.astNode())) {
								String name = namespaceKey.getName();

								if (((NameTok) node.attr).id.equals(name)) {
									positions
											.add(new ExpressionPosition<Attribute>(
													new ModelSite<Attribute>(
															node, codeBlock,
															namespaceKey
																	.getModel())));
								}
							}
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
		}.walk(codeObjectReferenceSite.codeObject());
	}

	private void addAccessesToNamespaceEntry(
			Set<ModelSite<? extends exprType>> moduleReferenceExpressions,
			Set<FlowPosition> positions) {
		for (ModelSite<? extends exprType> moduleReference : moduleReferenceExpressions) {
			searchScopeForAccessToNamespaceEntry(moduleReference.codeObject(),
					namespaceKey.getName(), moduleReference.astNode(),
					positions);
		}
	}

	/**
	 * Search for any accesses the the given attribute name on the given
	 * namespace key.
	 * 
	 * @param scope
	 * @param attributeName
	 * @param expression
	 * @param positions
	 */
	private void searchScopeForAccessToNamespaceEntry(final Namespace scope,
			final String attributeName, final exprType expression,
			final Set<FlowPosition> positions) {

		new CodeObjectWalker() {

			@Override
			protected void visitCodeObject(final Namespace codeBlock) {
				try {
					codeBlock.asCodeBlock().accept(new LocalCodeBlockVisitor() {

						@Override
						public Object visitAttribute(Attribute node)
								throws Exception {
							if (((NameTok) node.attr).id.equals(attributeName)
									&& node.value.equals(expression)) {
								positions
										.add(new ExpressionPosition<Attribute>(
												new ModelSite<Attribute>(node,
														codeBlock, namespaceKey
																.getModel())));
							}

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
		}.walk(scope);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((namespaceKey == null) ? 0 : namespaceKey.hashCode());
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
		NamespaceKeyFlowStepGoal other = (NamespaceKeyFlowStepGoal) obj;
		if (namespaceKey == null) {
			if (other.namespaceKey != null)
				return false;
		} else if (!namespaceKey.equals(other.namespaceKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceKeyFlowStepGoal [namespaceKey=" + namespaceKey + "]";
	}

}
