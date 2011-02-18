package uk.ac.ic.doc.gander.flowinference.types;

import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.aliasType;

public class TypeResolutionVisitor extends VisitorBase {

	// XXX: We are mapping strings to types. This isn't enough for real code.
	// As well as the string name of the token we're resolving, we need the
	// scope in which it appears as different names in different scopes
	// may refer to completely different things.
	private Map<String, Type> inferredTypes = new HashMap<String, Type>();

	public TypeResolutionVisitor(SimpleNode ast) throws Exception {
		ast.accept(this);
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		for (aliasType alias : node.names) {
			if (alias.asname != null)
				inferredTypes.put(((NameTok)alias.asname).id, new Module());
			else
				inferredTypes.put(((NameTok)alias.name).id, new Module());
		}
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
	
	public Type typeOf(String name) {
		return inferredTypes.get(name);
	}

}
