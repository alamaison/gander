package uk.ac.ic.doc.gander.model.codeblock;

import java.util.Set;

import org.python.pydev.parser.jython.ast.VisitorIF;

import uk.ac.ic.doc.gander.model.codeobject.FormalParameters;

public class DefaultCodeBlock implements CodeBlock {

	public interface Acceptor {
		void accept(VisitorIF visitor) throws Exception;
	}

	private Set<String> globals = null;
	private Set<String> boundNames = null;
	private final FormalParameters formalParameters;
	private final Acceptor acceptor;

	public DefaultCodeBlock(FormalParameters formalParameters, Acceptor acceptor) {
		if (formalParameters == null)
			throw new NullPointerException(
					"Must have parameter list even if it's empty");
		if (acceptor == null)
			throw new NullPointerException("Acceptor not optional");

		this.formalParameters = formalParameters;
		this.acceptor = acceptor;
	}

	FormalParameters formalParameters() {
		return formalParameters;
	}

	@Override
	public void accept(VisitorIF visitor) throws Exception {
		acceptor.accept(visitor);
	}

	@Override
	public Set<String> getGlobals() {
		if (globals == null) {
			globals = CodeBlockGlobalsFinder.globals(this);
		}
		return globals;
	}

	@Override
	public Set<String> getBoundVariables() {
		if (boundNames == null) {
			boundNames = CodeBlockBoundVariablesFinder.boundVariables(this);
		}
		return boundNames;
	}
}
