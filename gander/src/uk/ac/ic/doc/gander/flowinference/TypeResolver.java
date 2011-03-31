package uk.ac.ic.doc.gander.flowinference;

import java.util.Map;
import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.flowinference.types.TNamespace;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

public class TypeResolver extends VisitorBase {

	private Stack<Namespace> scopes = new Stack<Namespace>();
	private SymbolTable table;

	public TypeResolver(Model model) throws Exception {
		this.table = new SymbolTable(model);
	}

	public Type typeOf(SimpleNode node, Namespace scope) throws Exception {
		try {
			scopes.push(scope);
			return (Type) node.accept(this);
		} finally {
			scopes.pop();
		}
	}

	@Override
	public Object visitName(Name node) throws Exception {
		return new LexicalTokenResolver(scopes.peek(), table)
				.resolveToken(node.id);
	}

	@Override
	public Object visitAttribute(Attribute node) throws Exception {

		Type valueType = typeOf(node.value, scopes.peek());

		if (valueType != null && valueType instanceof TNamespace) {
			try {
				Namespace scope = ((TNamespace) valueType)
						.getNamespaceInstance();
				return table.symbols(scope).get(((NameTok) node.attr).id);
			} catch (UnresolvedImportError e) {
			}
		}

		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Don't traverse by default
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

	private static class LexicalTokenResolver {

		private Namespace scope;
		private SymbolTable table;

		public LexicalTokenResolver(Namespace scope, SymbolTable table) {
			this.scope = scope;
			this.table = table;
		}

		public Type resolveToken(String token) {
			return resolveTokenInScopeRecursively(token, scope);
		}

		private Type resolveTokenInScopeRecursively(String token,
				Namespace scope) {
			Type type = null;

			if (scope != null) {
				Map<String, Type> scopeTokens = table.symbols(scope);
				if (scopeTokens != null)
					type = scopeTokens.get(token);
				if (type == null)
					type = resolveTokenInScopeRecursively(token, scope
							.getParentScope());
			}

			return type;
		}
	}

}