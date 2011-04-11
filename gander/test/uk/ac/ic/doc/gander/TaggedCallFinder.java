package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.exprType;

public class TaggedCallFinder {
	private String variableName;
	private String methodName;
	private String tag;
	private Call call = null;

	public TaggedCallFinder(SimpleNode node, String variableName,
			String methodName, String tag) throws Exception {
		this.variableName = variableName;
		this.methodName = methodName;
		this.tag = tag;
		node.accept(new TagFinder());
	}

	boolean isFound() {
		return call != null;
	}

	public Call getTaggedCall() {
		return call;
	}

	private class TagFinder extends VisitorBase {

		@Override
		public Object visitCall(Call node) throws Exception {
			Call matchingCall = MethodCallHelper.matchMethodCall(variableName,
					methodName, node);
			if (matchingCall != null && paramsContainTag(matchingCall))
				call = matchingCall;

			// Calls may contain other calls as parameters so continue
			// digging into AST
			node.traverse(this);
			return null;
		}

		private boolean paramsContainTag(Call call) {
			if (call.args.length < 1)
				return false;

			for (exprType arg : call.args) {
				if (((Str) arg).s.equals(tag))
					return true;
			}

			return false;
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			node.traverse(this);
		}
	}
}