package uk.ac.ic.doc.gander.model.codeobject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.DictComp;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.GeneratorExp;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.SetComp;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;

final class NestedCodeObjectFinder {

	private final CodeObject enclosingCodeObject;
	private final Set<CodeObject> nestedCodeObjects = new HashSet<CodeObject>();
	private final Model model;

	NestedCodeObjectFinder(SimpleNode ast, CodeObject enclosingCodeObject,
			Model model) {
		assert ast != null;
		assert enclosingCodeObject != null;
		assert model != null;

		this.enclosingCodeObject = enclosingCodeObject;
		this.model = model;

		try {
			/*
			 * We ignore the top of the tree because that is the current code
			 * block so we traverse rather than accept
			 */
			ast.traverse(new Finder());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	Set<CodeObject> codeObjects() {
		return Collections.unmodifiableSet(nestedCodeObjects);
	}

	private class Finder extends VisitorBase {

		@Override
		public Object visitClassDef(ClassDef node) throws Exception {
			ClassCO classCodeObject = new ClassCO(node, enclosingCodeObject);
			nestedCodeObjects.add(classCodeObject);
			Class namespace = new Class(classCodeObject, model);
			classCodeObject.setNamespace(namespace);
			namespace.addNestedCodeObjects();

			/*
			 * we only want the immediately nested code objects to don't
			 * traverse further
			 */
			return null;
		}

		@Override
		public Object visitFunctionDef(FunctionDef node) throws Exception {
			FunctionCO functionCodeObject = new FunctionCO(node,
					enclosingCodeObject);
			nestedCodeObjects.add(functionCodeObject);
			Function namespace = new Function(functionCodeObject, model);
			functionCodeObject.setNamespace(namespace);
			namespace.addNestedCodeObjects();

			/*
			 * we only want the immediately nested code objects to don't
			 * traverse further
			 */
			return null;
		}

		@Override
		public Object visitDictComp(DictComp node) throws Exception {
			// TODO: comprehension creates new, anonymous, code object
			return null;
		}

		@Override
		public Object visitGeneratorExp(GeneratorExp node) throws Exception {
			// TODO: comprehension creates new, anonymous, code object
			return null;
		}

		@Override
		public Object visitSetComp(SetComp node) throws Exception {
			// TODO: comprehension creates new, anonymous, code object
			return null;
		}

		@Override
		public Object visitLambda(Lambda node) throws Exception {
			// TODO: lambda creates new, anonymous, code object
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			/* code object declarations may be nested in if/while/etc */
			node.traverse(this);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

	}

}
