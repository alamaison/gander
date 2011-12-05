package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.aliasType;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;

/**
 * Detector of statements able to bind a value.
 * 
 * When applied to an AST node it doesn't traverse the tree. It just detects any
 * bindings caused by that node.
 */
public final class BindingDetector extends BindingStatementVisitor {

	private final DetectionEvent eventHandler;

	public interface DetectionEvent {
		boolean assignment(exprType lhs, exprType rhs);

		void function(String name, FunctionDef node);

		void classDefiniton(String name, ClassDef node);

		void forLoop(exprType target, exprType iterable);

		boolean moduleImport(String moduleName);

		boolean moduleImportAs(String moduleName, String as);

		boolean fromModuleImport(String moduleName, String itemName);

		boolean fromModuleImportAs(String moduleName, String itemName, String as);

		boolean exception(exprType name, exprType type);
	}

	public BindingDetector(DetectionEvent eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		for (exprType lhs : node.targets) {

			if (eventHandler.assignment(lhs, node.value))
				break;
		}

		return null;
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		eventHandler.function(((NameTok) node.name).id, node);
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
	public Object visitImport(Import node) throws Exception {
		for (aliasType alias : node.names) {

			String moduleName = ((NameTok) alias.name).id;

			if (alias.asname == null) {
				if (eventHandler.moduleImport(moduleName)) {
					break;
				}
			} else {
				String as = ((NameTok) alias.asname).id;
				if (eventHandler.moduleImportAs(moduleName, as)) {
					break;
				}
			}
		}
		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {

		for (aliasType alias : node.names) {

			String moduleName = ((NameTok) node.module).id;
			String itemName = ((NameTok) alias.name).id;

			if (alias.asname == null) {
				if (eventHandler.fromModuleImport(moduleName, itemName)) {
					break;
				}
			} else {
				String as = ((NameTok) alias.asname).id;
				if (eventHandler.fromModuleImportAs(moduleName, itemName, as)) {
					break;
				}
			}
		}

		return null;
	}

	@Override
	public Object visitTryExcept(TryExcept node) throws Exception {

		for (excepthandlerType handler : node.handlers) {
			if (eventHandler.exception(handler.name, handler.type))
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
