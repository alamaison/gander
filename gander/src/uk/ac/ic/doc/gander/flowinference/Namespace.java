package uk.ac.ic.doc.gander.flowinference;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * The Python runtime memory model.
 * 
 * Namespaces are the essential item in the Python memory model. Variables just
 * read and write to their binding namespace and attributes read or write to one
 * or more namespaces exposed by an object.
 */
public interface Namespace {

	/**
	 * Returns the expressions that expose this namespace.
	 * 
	 * In other words, the expressions that may hold an object that, via an
	 * attribute access, can read values from names of this namespace.
	 * 
	 * There is no such thing as a write-only namespace so this is a superset of
	 * the writeable references.
	 * 
	 * @param goalManager
	 *            allows us to use type inference to determine the result.
	 */
	public Result<ModelSite<exprType>> references(SubgoalManager goalManager);

	/**
	 * Returns the expressions that expose this namespace such that its members
	 * may be modified.
	 * 
	 * In other words, the expressions that may hold an object that, via a
	 * binding to attribute access, can set names of this namespace.
	 * 
	 * @param goalManager
	 *            allows us to use type inference to determine the result.
	 */
	public Result<ModelSite<exprType>> writeableReferences(
			SubgoalManager goalManager);

	/**
	 * Returns the set of variables that can read the value of the given name in
	 * this namespace.
	 */
	public Set<Variable> variablesInScope(String name);

	/**
	 * Returns the set of variables that can set the value of the given name in
	 * this namespace.
	 */
	public Set<Variable> variablesWriteableInScope(String name);

	/**
	 * Returns the runtime model of the system under which this namespace
	 * instance is valid.
	 */
	public Model model();
}
