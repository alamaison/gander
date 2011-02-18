package uk.ac.ic.doc.gander.cfg.model;

import java.util.HashMap;
import java.util.Map;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.stmtType;

public class Class implements IModelElement {

	private ClassDef cls;
	private IModelElement parent;

	protected Class(ClassDef cls, IModelElement parent) {
		this.cls = cls;
		this.parent = parent;
	}

	public String getName() {
		return ((NameTok) (cls.name)).id;
	}
	
	public String getFullName() {
		return parent.getFullName() + "." + getName();
	}

	public Map<String, Method> getMethods() {
		Map<String, Method> methods = new HashMap<String, Method>();
		for (stmtType stmt : cls.body) {
			if (stmt instanceof FunctionDef) {
				Method method = new Method((FunctionDef) stmt, this);
				methods.put(method.getName(), method);
			}
		}

		return methods;
	}

}
