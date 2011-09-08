package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.ast.ScopedAstVisitor;
import uk.ac.ic.doc.gander.model.Namespace;

public class TaggedCallAndScopeFinder {
	private String tag;
	private Call call = null;
	private Namespace scope;

	public TaggedCallAndScopeFinder(Namespace startingScope, String tag)
			throws Exception {
		this.tag = tag;
		startingScope.getAst().accept(new TagFinder(startingScope));
	}

	public Call getTaggedCall() {
		return call;
	}

	public Namespace getCallScope() {
		return scope;
	}

	private class TagFinder extends ScopedAstVisitor<Namespace> {

		public TagFinder(Namespace startingScope) {
			super(startingScope);
		}

		@Override
		public Object visitCall(Call node) throws Exception {

			if (TaggedCallFinder.paramsContainTag(node, tag)) {
				call = node;
				scope = getScope();
			} else {
				// Calls may contain other calls as parameters so continue
				// digging into AST
				node.traverse(this);
			}
			return null;
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}

		@Override
		protected Namespace atScope(Module node) {
			assert getScope().getAst() == node;
			return getScope();
		}

		@Override
		protected Namespace atScope(FunctionDef node) {
			return getScope().getFunctions().get(((NameTok) node.name).id);
		}

		@Override
		protected Namespace atScope(ClassDef node) {
			return getScope().getClasses().get(((NameTok) node.name).id);
		}
	}
}