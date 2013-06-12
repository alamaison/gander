package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.VisitorBase;

/**
 * Visitor that handles any statement that starts a new code block.
 */
public abstract class CodeBlockCreationVisitor extends VisitorBase {

	@Override
	public abstract Object visitClassDef(ClassDef node) throws Exception;

	@Override
	public abstract Object visitFunctionDef(FunctionDef node) throws Exception;

	@Override
	public abstract Object visitModule(
			org.python.pydev.parser.jython.ast.Module node) throws Exception;

	// TODO: Investigate if lambdas belong here

}

