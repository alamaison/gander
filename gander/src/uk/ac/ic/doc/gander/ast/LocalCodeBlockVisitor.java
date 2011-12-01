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
 * override {@link seenNestedClassDef} and {@link seenNestedFunctionDef} but not
 * traverse the node bodies.
 */
public abstract class LocalCodeBlockVisitor extends CodeBlockCreationVisitor {

	protected Object seenNestedClassDef(ClassDef node) throws Exception {
		return null;
	}

	protected Object seenNestedFunctionDef(FunctionDef node) throws Exception {
		return null;
	}

	// TODO: Add similar for Lambda and generator expressions

	@Override
	public final Object visitClassDef(ClassDef node) throws Exception {
		return seenNestedClassDef(node);
		/*
		 * Note existence for name binding purposes but do not traverse into
		 * foreign code block
		 */
	}

	@Override
	public final Object visitFunctionDef(FunctionDef node) throws Exception {
		return seenNestedFunctionDef(node);
		/*
		 * Note existence for name binding purposes but do not traverse into
		 * foreign code block
		 */
	}

	@Override
	public final Object visitModule(
			org.python.pydev.parser.jython.ast.Module node) throws Exception {
		return null;
	}

}
