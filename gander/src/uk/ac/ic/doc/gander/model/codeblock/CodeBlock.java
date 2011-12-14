package uk.ac.ic.doc.gander.model.codeblock;

import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public interface CodeBlock {

	void accept(VisitorIF visitor) throws Exception;

	/**
	 * Returns the set of names that are declared global.
	 * 
	 * This is not the same thing as the names that bind globally. Some names
	 * may be free in this code block but bind globally because the only
	 * definition in scope occurs in the global code block.
	 */
	Set<String> getGlobals();

	/**
	 * Returns the set of names that bound in this code block.
	 * 
	 * This is not the same thing as the local variables of the block as they
	 * may be the subject of a 'global' declaration in that block. It also
	 * doesn't include bindings that occur in declarations such as nested
	 * functions and classes that create a new code block.
	 */
	Set<String> getBoundVariables();

	/**
	 * Return the names that parameters are bound to locally.
	 * 
	 * Order is significant for deciding the types of the parameters so they are
	 * returned as a list in order.
	 * 
	 * XXX: This is a symptom of our conflated binding and type inference. When
	 * we separate these we can change this to a set which makes more sense
	 * (parameters can't occur twice with the same name - or can they).
	 */
	List<String> getNamedFormalParameters();
}
