package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;


/**
 * Visitor that only visits statements that are logically part of the local code
 * block as defined by the Python spec (section 4).
 * 
 * The AST associated with a code block can contain nodes that begin a new code
 * block. When the task at hand only makes sense in the context of the current
 * code block, subclass this class to ensure the analysis doesn't traverse into
 * another code block.
 * 
 * Note, a subclass of this visitor won't even see the class/function
 * declarations in the current scope. A subclasses that needs this should
 * subclass {@link CodeBlockCreationVisitor} and override {@code visitClassDef}
 * or {@code visitFunctionDef} but not traverse their node bodies.
 */
public abstract class LocalCodeBlockVisitor extends CodeBlockCreationVisitor {

	@Override
	public final Object visitClassDef(ClassDef node) throws Exception {
		return null;
	}

	@Override
	public final Object visitFunctionDef(FunctionDef node) throws Exception {
		return null;
	}

	@Override
	public final Object visitModule(
			org.python.pydev.parser.jython.ast.Module node) throws Exception {
		return null;
	}

}

