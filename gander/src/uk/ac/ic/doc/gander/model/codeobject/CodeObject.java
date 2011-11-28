package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Set;

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
	 * Return the module in which this code object appears.
	 * 
	 * All code objects appear in a module. For a module code object this is
	 * simply the module itself. Knowing the module is a useful thing because it
	 * defines the identity of the global namespace for everything within it.
	 */
	ModuleCO enclosingModule();

	/**
	 * Return the code objects nested within this one.
	 */
	Set<CodeObject> nestedCodeObjects();

	/**
	 * Returns the code object whose namespace is the next candidate for
	 * lexically binding variables.
	 * 
	 * This is the enclosing code object for everything but classes.
	 * 
	 * We need this because the lexical scoping rules in Python aren't orthodox.
	 * Variables defined in a class body don't have scope outside the class's
	 * code block. Names used in nested classes or functions (methods) aren't
	 * bound in the class's namespace. Instead they are bound in the namespace
	 * of the next enclosing non-class code object.
	 * 
	 * @return the code object whose namespace is the immediate lexical parent
	 *         of this one or {@code null} if no such parent exists
	 */
	CodeObject lexicallyNextCodeObject();

	/**
	 * Temporary hack to get at the namespace.
	 */
	@Deprecated
	Namespace oldStyleConflatedNamespace();

	/**
	 * Do variables in nested code objects consider this code object's namespace
	 * when deciding which namespace they lexically bind in?
	 */
	boolean nestedVariablesCanBindHere();
}