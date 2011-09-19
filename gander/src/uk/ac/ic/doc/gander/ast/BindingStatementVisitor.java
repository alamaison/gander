package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.VisitorBase;


/**
 * Visitor that handles statements that can bind a name.
 * 
 * From PEP227: The following operations are name binding operations. If they
 * occur within a block, they introduce new local names in the current block
 * unless there is also a global declaration.
 * 
 * Function definition: def name ...
 * 
 * Argument declaration: def f(...name...), lambda ...name...
 * 
 * Class definition: class name ...
 * 
 * Assignment statement: name = ...
 * 
 * Import statement: import name, import module as name, from module import name
 * 
 * Implicit assignment: names are bound by for statements and except clauses
 * 
 */
public abstract class BindingStatementVisitor extends VisitorBase {

	/**
	 * This is triggered when a function is defined.
	 * 
	 * e.g:
	 * 
	 * <pre>
	 * def fun(x):
	 *     pass
	 * </pre>
	 */
	@Override
	public abstract Object visitFunctionDef(FunctionDef node) throws Exception;

	@Override
	public abstract Object visitAssign(Assign node) throws Exception;

	@Override
	public abstract Object visitClassDef(ClassDef node) throws Exception;

	@Override
	public abstract Object visitFor(For node) throws Exception;

	@Override
	public abstract Object visitImport(Import node) throws Exception;

	@Override
	public abstract Object visitImportFrom(ImportFrom node) throws Exception;

	@Override
	public abstract Object visitTryExcept(TryExcept node) throws Exception;

}