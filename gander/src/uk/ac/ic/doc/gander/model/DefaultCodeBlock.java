package uk.ac.ic.doc.gander.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.VisitorIF;

import uk.ac.ic.doc.gander.model.name_binding.CodeBlockGlobalsFinder;

class DefaultCodeBlock implements CodeBlock {

	interface Acceptor {
		void accept(VisitorIF visitor) throws Exception;
	}

	private Set<String> globals = null;
	private final List<String> formalParameters;
	private final Acceptor acceptor;

	DefaultCodeBlock(List<String> formalParameters, Acceptor acceptor) {
		this.formalParameters = new ArrayList<String>(formalParameters);
		this.acceptor = acceptor;
	}

	public List<String> getFormalParameters() {
		return Collections.unmodifiableList(this.formalParameters);
	}

	public void accept(VisitorIF visitor) throws Exception {
		acceptor.accept(visitor);
	}

	public Set<String> getGlobals() {
		if (globals == null) {
			globals = CodeBlockGlobalsFinder.globals(this);
		}
		return globals;
	}

}
