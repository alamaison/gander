package uk.ac.ic.doc.gander.model.codeblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public class DefaultCodeBlock implements CodeBlock {

	public interface Acceptor {
		void accept(VisitorIF visitor) throws Exception;
	}

	private Set<String> globals = null;
	private Set<String> boundNames = null;
	private final List<ModelSite<exprType>> formalParameters;
	private final Acceptor acceptor;

	public List<ModelSite<exprType>> getFormalParameters() {
		return Collections.unmodifiableList(formalParameters);
	}

	public DefaultCodeBlock(List<ModelSite<exprType>> formalParameters,
			Acceptor acceptor) {
		if (formalParameters == null)
			throw new NullPointerException(
					"Must have parameter list even if it's empty");
		if (acceptor == null)
			throw new NullPointerException("Acceptor not optional");

		this.formalParameters = new ArrayList<ModelSite<exprType>>(
				formalParameters);
		this.acceptor = acceptor;
	}

	public List<String> getNamedFormalParameters() {
		ArrayList<String> args = new ArrayList<String>();
		for (ModelSite<exprType> expr : formalParameters) {
			if (expr.astNode() instanceof Name) {
				args.add(((Name) expr.astNode()).id);
			} else {
				/*
				 * TODO: Work out what we want to do ... and why this even
				 * happens. I've seen this be a Tuple, for instance.
				 */
			}
		}
		return args;
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

	public Set<String> getBoundVariables() {
		if (boundNames == null) {
			boundNames = CodeBlockBoundVariablesFinder.boundVariables(this);
		}
		return boundNames;
	}

}
