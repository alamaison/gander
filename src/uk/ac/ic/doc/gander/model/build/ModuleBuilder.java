package uk.ac.ic.doc.gander.model.build;

import java.io.File;
import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Package;

public class ModuleBuilder {

	private class ModuleVisitor extends VisitorBase {

		private Stack<BuildableScope> scopes = new Stack<BuildableScope>();

		@Override
		public Object visitModule(Module node) throws Exception {
			module = new uk.ac.ic.doc.gander.model.Module(node, name, parentPackage);
			parentPackage.addModule(module);
			
			scopes.push(module);
			node.traverse(this);
			scopes.pop();
			assert scopes.isEmpty();
			
			return null;
		}
		
		@Override
		public Object visitClassDef(ClassDef node) throws Exception {
			Class klass = new Class(node, scopes.peek());
			scopes.peek().addClass(klass);
			
			scopes.push(klass);
			node.traverse(this);
			scopes.pop();
			
			return null;
		}

		@Override
		public Object visitFunctionDef(FunctionDef node) throws Exception {
			Function function = new Function(node, scopes.peek());
			scopes.peek().addFunction(function);
			
			scopes.push(function);
			node.traverse(this);
			scopes.pop();
			
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

	}

	private uk.ac.ic.doc.gander.model.Module module;
	private String name;
	private Package parentPackage;

	public ModuleBuilder(File module, Package parent) throws Exception {
		this.parentPackage = parent;
		ModuleParser parser = new ModuleParser(module);
		this.name = parser.getName();
		parser.getAst().accept(new ModuleVisitor());
	}

	public uk.ac.ic.doc.gander.model.Module getModule() {
		return module;
	}

}
