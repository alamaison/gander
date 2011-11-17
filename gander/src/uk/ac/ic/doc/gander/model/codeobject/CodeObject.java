package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

public interface CodeObject {

	/**
	 * Returns the AST node of the code object
	 */
	SimpleNode ast();

	/**
	 * Returns the code block with the executable statements in the code object
	 */
	CodeBlock codeBlock();
	
	/**
	 * Returns the {@link Model} in whose context this code object is valid.
	 */
	Model model();
	
	/**
	 * Temporary hack to get at the namespace.
	 */
	@Deprecated
	Namespace oldStyleConflatedNamespace();
}
