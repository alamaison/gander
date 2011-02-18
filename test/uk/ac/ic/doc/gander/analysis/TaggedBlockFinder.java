package uk.ac.ic.doc.gander.analysis;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;

public class TaggedBlockFinder {

	private static Pattern taggedCallPattern = Pattern.compile("(\\w+)\\.(\\w+)\\(\\\"?(\\w+)\\\"?\\)");

	private static class TagFinder extends VisitorBase {

		private String variable;
		private String method;
		private String tag;
		private Call call = null;

		TagFinder(SimpleNode node, String variable, String method,
				String tag) throws Exception {
			this.variable = variable;
			this.method = method;
			this.tag = tag;
			node.accept(this);
		}

		@Override
		public Object visitCall(Call node) throws Exception {
			Call call = matchMethodCall(variable, method, node);
			if (call != null && call.args.length > 0
					&& ((Str) call.args[0]).s.equals(tag)) {
				this.call = call;
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

		boolean isFound() {
			return call != null;
		}

		Call getTaggedCall() {
			return call;
		}
	}

	private Cfg graph;

	public TaggedBlockFinder(Cfg graph) {
		this.graph = graph;
	}

	public Statement findTaggedStatement(String variable, String method,
			String tag) throws Exception {
		Set<BasicBlock> blocks = graph.getBlocks();
		for (BasicBlock block : blocks) {
			for (SimpleNode statement : block) {
				TagFinder finder = new TagFinder(statement, variable, method,
						tag);
				if (finder.isFound())
					return new Statement(finder.getTaggedCall(), block);
			}
		}

		return null; // statement not found
	}
	
	private Matcher match(String taggedCall) {
		Matcher matcher = taggedCallPattern.matcher(taggedCall);
		matcher.find();
		return matcher;
	}
	
	public String variableFromTag(String taggedCall) {
		return match(taggedCall).group(1);
	}
	
	public String methodFromTag(String taggedCall) {
		return match(taggedCall).group(2);
	}
	
	public String tagFromTag(String taggedCall) {
		return match(taggedCall).group(3);
	}

	public Statement findTaggedStatement(String taggedCall) throws Exception {
		Matcher matcher = match(taggedCall);
		return findTaggedStatement(matcher.group(1), matcher.group(2), matcher.group(3));
	}

	/**
	 * Converts given statement into method call if it matches the given
	 * criteria.
	 * 
	 * @param variable
	 *            Expected variable name.
	 * @param method
	 *            Expected method name.
	 * @param statement
	 *            Statement to convert.
	 * @return statement converted to a Call if it matches.
	 * @throws ClassCastException
	 *             if the statement isn't a method call.
	 */
	private static Call matchMethodCall(String variable, String method,
			SimpleNode statement) {
		Call call = (Call) statement;
		if (isMethodCallTarget(variable, call)
				&& isMethodCallName(method, call))
			return call;
		else
			return null; // call doesn't match criteria
	}

	private static Name matchMethodCallTarget(String variable, Call call) {
		Name target = extractMethodCallTarget(call);
		if (target.id.equals(variable))
			return target;
		else
			return null;
	}

	private static NameTok matchMethodCallName(String methodName, Call call) {
		NameTok name = extractMethodCallName(call);
		if (name.id.equals(methodName))
			return name;
		else
			return null;
	}

	static boolean isMethodCallTarget(String variable, Call call) {
		try {
			return matchMethodCallTarget(variable, call) != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	private static boolean isMethodCallName(String method, Call call) {
		try {
			return matchMethodCallName(method, call) != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	static boolean isMethodCall(String variable, String method, Call call) {
		try {
			return matchMethodCall(variable, method, call) != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	static Name extractMethodCallTarget(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (Name) fieldAccess.value;
	}

	static NameTok extractMethodCallName(Call call) {
		Attribute fieldAccess = (Attribute) call.func;
		return (NameTok) fieldAccess.attr;
	}
}