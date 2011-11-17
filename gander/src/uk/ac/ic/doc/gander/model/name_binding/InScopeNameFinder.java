package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Given a namespace key, finds the {@link Name}s that always bind to that
 * entry.
 * 
 * In other words, a subset of the {@link Name}s that alias the value of the
 * given name binding.
 * 
 * Many other expressions, such as attribute references that explicitly specify
 * the namespace, may alias the bound name's value but they are not found by
 * this class. Other code objects (or even in bizarre cases, this code object)
 * may even alias this bound name's value via a {@link Name} that doesn't
 * lexically bind in this namespace, either directly using a from-style import
 * or indirectly by assigning the result of another expression that aliases this
 * name, but these are also not included in the names found by this class.
 * 
 * XXX: from-style import names always bind to the given entry, don't they?
 * 
 * Names can be shadowed by local variable declarations or global statements in
 * their code block. This class essentially filters those out.
 */
public final class InScopeNameFinder {

	private final Set<ModelSite<Name>> nameBindings = new HashSet<ModelSite<Name>>();

	public InScopeNameFinder(final NamespaceKey namespaceKey) {

		CodeObjectWalker walker = new CodeObjectWalker() {

			@Override
			protected void visitCodeObject(Namespace codeBlock) {
				analyseCodeBlock(namespaceKey, codeBlock);
			}

		};

		// Name bindings will only ever appear in or below the namespace they
		// bind in so it it ok to start the search here rather than at the root
		// of the model
		walker.walk(namespaceKey.getNamespace()); // .walk(namespace.getCodeObject())
	}

	/**
	 * Returns those {@link Name}s that bind to the given namespace key.
	 * 
	 * @return the set model sites for the bound {@link Name}s.
	 */
	public Set<ModelSite<Name>> getNameBindings() {
		return nameBindings;
	}

	private void analyseCodeBlock(final NamespaceKey nameBinding,
			Namespace codeBlock) {
		if (nameBindingIsActiveInCodeBlock(nameBinding, codeBlock)) {
			addAllNameInstances(nameBinding.getName(), codeBlock, nameBinding
					.getModel());
		}
	}

	private void addAllNameInstances(final String name,
			final Namespace codeBlock, final Model model) {
		try {
			codeBlock.asCodeBlock().accept(new LocalCodeBlockVisitor() {

				@Override
				public Object visitName(Name node) throws Exception {
					if (node.id.equals(name)) {
						nameBindings.add(new ModelSite<Name>(node, codeBlock));
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
					// name may be deeply nested so traverse
					node.traverse(this);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Is the given name binding the active binding for that name in the given
	 * code block or do its instances of that name bind in some other namespace?
	 * 
	 * @param nameBinding
	 *            name binding whose scope we are establishing
	 * @param codeBlock
	 *            code block in which we want to establish the given name
	 *            binding's validity
	 * @return {@code true} if the given name binding is the active binding for
	 *         that name in the given code block; {@code false} otherwise
	 */
	private boolean nameBindingIsActiveInCodeBlock(NamespaceKey nameBinding,
			Namespace codeBlock) {
		NamespaceKey otherBinding = Binder.resolveBindingScope(nameBinding
				.getName(), codeBlock);
		return otherBinding.getNamespace().equals(nameBinding.getNamespace());
	}

}
