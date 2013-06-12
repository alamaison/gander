package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.commentType;

import uk.ac.ic.doc.gander.ast.ScopedAstVisitor;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TaggedNodeAndScopeFinder {
	private int taggedLine = 0;
	private ScopedAstNode taggedNode = null;

	public TaggedNodeAndScopeFinder(CodeObject startingScope, String tag)
			throws Exception {
		startingScope.ast().accept(new TagFinder(tag));
		startingScope.ast().accept(new LineFinder(startingScope));
	}

	public ScopedAstNode getTaggedNode() {
		return taggedNode;
	}

	/**
	 * Find line of source code tagged with given tag string.
	 */
	private class TagFinder extends VisitorBase {

		private String tag;

		TagFinder(String tag) {
			this.tag = tag;
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			if (taggedLine == 0 && node.specialsAfter != null) {
				for (Object special : node.specialsAfter) {
					if (special instanceof commentType) {
						String comment = ((commentType) special).id.replaceAll(
								"^[ \\t#]*|[ \\t]*$", "");
						if (comment.equals(tag)) {
							taggedLine = node.beginLine;
							return null;
						}
					}
				}
			}

			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			if (taggedLine == 0)
				node.traverse(this);
		}
	}

	/**
	 * Find the least-nested AST node that occurs on the tagged line.
	 */
	private class LineFinder extends ScopedAstVisitor<CodeObject> {

		public LineFinder(CodeObject startingScope) {
			super(startingScope);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			if (taggedNode == null && node.beginLine == taggedLine)
				taggedNode = new ScopedAstNode(node, getScope());
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			if (taggedNode == null)
				node.traverse(this);
		}

		@Override
		protected CodeObject atScope(Module node) {
			assert getScope().ast() == node;
			return getScope();
		}

		@Override
		protected CodeObject atScope(FunctionDef node) {
			CodeObject function = getScope().nestedCodeObjects()
					.findCodeObjectMatchingAstNode(node);

			if (function != null) {
				return function;
			} else {
				throw new AssertionError("Mismatch between code object "
						+ "model and AST; no code object found for node "
						+ node + " in " + getScope());
			}
		}

		@Override
		protected CodeObject atScope(ClassDef node) {
			CodeObject klass = getScope().nestedCodeObjects()
					.findCodeObjectMatchingAstNode(node);

			if (klass != null) {
				return klass;
			} else {
				throw new AssertionError("Mismatch between code object "
						+ "model and AST; no code object found for node "
						+ node + " in " + getScope());
			}
		}
	}
}