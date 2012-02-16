package uk.ac.ic.doc.gander.flowinference;

import java.util.Map;
import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.List;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Set;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TCodeObject;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.LexicalResolver;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.OldNamespace;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TypeResolver extends VisitorBase {

	private Stack<OldNamespace> scopes = new Stack<OldNamespace>();
	private SymbolTable table;
	private final Model model;

	public TypeResolver(Model model) {
		this.model = model;
		this.table = new SymbolTable(model);
	}

	public Type typeOf(SimpleNode node, OldNamespace scope) {
		try {
			scopes.push(scope);
			return (Type) node.accept(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			scopes.pop();
		}
	}

	@Override
	public Object visitName(Name node) throws Exception {
		Type type = new SymbolTableTypeResolver(table).resolveToken(node.id,
				scopes.peek().codeObject());
		if (type == null) {
			/* Token could not be resolved so look in the builtin namespace */
			type = table.symbols(model.getTopLevel()).get(node.id);
		}

		return type;
	}

	@Override
	public Object visitAttribute(Attribute node) throws Exception {

		Type valueType = typeOf(node.value, scopes.peek());

		if (valueType != null && valueType instanceof TCodeObject) {
			try {
				OldNamespace scope;
				if (valueType instanceof TUnresolvedImport) {
					scope = null;
				} else {
					scope = ((TCodeObject) valueType).codeObject()
							.oldStyleConflatedNamespace();
				}

				return table.symbols(scope).get(((NameTok) node.attr).id);
			} catch (UnresolvedImportError e) {
			}
		}

		return null;
	}

	@Override
	public Object visitStr(Str node) throws Exception {
		return new TClass(model.getTopLevel().getClasses().get("str"));
	}

	@Override
	public Object visitDict(Dict node) throws Exception {
		return new TClass(model.getTopLevel().getClasses().get("dict"));
	}

	@Override
	public Object visitList(List node) throws Exception {
		return new TClass(model.getTopLevel().getClasses().get("list"));
	}

	@Override
	public Object visitSet(Set node) throws Exception {
		return new TClass(model.getTopLevel().getClasses().get("set"));
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Don't traverse by default
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

	/**
	 * Token resolver using the existing symbol table to map names to types.
	 */
	private class SymbolTableTypeResolver extends LexicalResolver<Type> {

		private SymbolTable table;

		public SymbolTableTypeResolver(SymbolTable table) {
			this.table = table;
		}

		@Override
		protected Type searchScopeForVariable(String token, CodeObject scope) {
			Map<String, Type> scopeTokens = table.symbols(model
					.intrinsicNamespace(scope));
			if (scopeTokens != null)
				return scopeTokens.get(token);

			return null;
		}
	}

}
