package uk.ac.ic.doc.gander.model.name_binding;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Given a namespace key, finds the {@link Name}s that always bind to that
 * entry.
 * 
 * In other words, a subset of the {@link Name}s that alias the value of the
 * given name binding. Names can be shadowed by local variable declarations or
 * global statements in their code block. This class essentially filters those
 * out.
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
 */
public final class NameScopeFinder {

    private final Set<ModelSite<Name>> nameBindings = new HashSet<ModelSite<Name>>();

    public NameScopeFinder(NamespaceName name) {
        if (name == null) {
            throw new NullPointerException("Namespace name required");
        }

        /*
         * Name bindings will only ever appear in or below the namespace they
         * bind in so it it ok to start the search here rather than at the root
         * of the model
         */
        for (Variable variable : name.namespace().variablesInScope(name.name())) {
            addAllNameInstances(variable.name(), variable.codeObject());
        }
    }

    /**
     * Returns those {@link Name}s that bind to the given namespace key.
     * 
     * @return the set model sites for the bound {@link Name}s.
     */
    public Set<ModelSite<Name>> getNameBindings() {
        return nameBindings;
    }

    private void addAllNameInstances(final String name,
            final CodeObject codeObject) {
        try {
            codeObject.codeBlock().accept(new LocalCodeBlockVisitor() {

                @Override
                protected Object seenNestedClassDef(ClassDef node)
                        throws Exception {
                    // The list of classes the subclass inherits from is part of
                    // the enclosing code block, not the subclass code block
                    for (exprType base : node.bases) {
                        base.accept(this);
                    }
                    return null;
                }

                @Override
                protected Object seenNestedFunctionDef(FunctionDef node)
                        throws Exception {
                    // XXX: Do we need to handle anything special here?
                    return null;
                }

                @Override
                public Object visitName(Name node) throws Exception {
                    if (node.id.equals(name)) {
                        nameBindings.add(new ModelSite<Name>(node, codeObject));
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

}
