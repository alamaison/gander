package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Comprehension;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.ListComp;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.comprehensionType;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.importing.ImportStatement;
import uk.ac.ic.doc.gander.importing.ImportStatementFactory;

/**
 * Detector of statements able to bind a value.
 * 
 * When applied to an AST node it doesn't traverse the tree. It just detects any
 * bindings caused by that node.
 */
public final class BindingDetector extends BindingStatementVisitor {

	private final DetectionEvent eventHandler;

	public interface DetectionEvent {

		void assignment(exprType[] lhs, exprType rhs);

		void functionDefinition(String name, FunctionDef node);

		void classDefiniton(String name, ClassDef node);

		void forLoop(exprType target, exprType iterable);

		boolean importStatement(ImportStatement importation);

		boolean exceptionHandler(exprType name, exprType type);
	}

	public BindingDetector(DetectionEvent eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		eventHandler.assignment(node.targets, node.value);
		return null;
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		eventHandler.functionDefinition(((NameTok) node.name).id, node);
		return null;
	}

	@Override
	public Object visitClassDef(ClassDef node) throws Exception {
		eventHandler.classDefiniton(((NameTok) node.name).id, node);
		return null;
	}

	@Override
	public Object visitFor(For node) throws Exception {
		eventHandler.forLoop(node.target, node.iter);
		return null;
	}

	@Override
	public Object visitListComp(ListComp node) throws Exception {
		/*
		 * UNOBVIOUS: list comprehensions' temporary variables survive after the
		 * comprehension is complete. In other words, they just treat the temp
		 * var as though it were any other variable of that name in the code
		 * block.
		 */
		for (comprehensionType generator : node.generators) {
			eventHandler.forLoop(((Comprehension) generator).target,
					((Comprehension) generator).iter);
		}
		return null;
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		Iterable<ImportStatement> specifications = ImportStatementFactory
				.fromAstNode(node);

		for (ImportStatement specification : specifications) {

			if (eventHandler.importStatement(specification)) {
				break;
			}
		}

		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {

		Iterable<ImportStatement> specifications = ImportStatementFactory
				.fromAstNode(node);

		for (ImportStatement specification : specifications) {

			if (eventHandler.importStatement(specification)) {
				break;
			}
		}

		return null;
	}

	@Override
	public Object visitTryExcept(TryExcept node) throws Exception {

		for (excepthandlerType handler : node.handlers) {
			if (eventHandler.exceptionHandler(handler.name, handler.type))
				break;
		}
		return null;
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Don't traverse, just map
	}
}
