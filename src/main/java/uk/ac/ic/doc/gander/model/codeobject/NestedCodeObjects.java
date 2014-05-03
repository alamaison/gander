package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Set;

import org.python.pydev.parser.jython.ast.stmtType;

/**
 * Collection of code objects appearing nested within another.
 */
public interface NestedCodeObjects extends Set<NestedCodeObject> {

	/**
	 * Finds the nested code object whose AST node matches the given one.
	 * 
	 * @param ast
	 *            node of code object to search for
	 * @return the corresponding nested code object if found,
	 *         {@code null otherwise}
	 */
	NestedCodeObject findCodeObjectMatchingAstNode(stmtType ast);

	/**
	 * Finds the nested code objects that are declared with the given name.
	 * 
	 * This will usually be one object but may be more.
	 */
	NestedCodeObjects namedCodeObjectsDeclaredAs(String declaredName);

}
