package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TUnresolvedImport implements TCodeObject {

	private Import<?, ?, ?> importInstance;

	public TUnresolvedImport(Import<?, ?, ?> importInstance) {
		if (importInstance == null)
			throw new NullPointerException("Failed import not optional");
		this.importInstance = importInstance;
	}

	public CodeObject codeObject() {
		return null;
	}

	public String getName() {
		return "<unresolved import: '" + importInstance + "'>";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on an unresolved import cannot be accurately typed so we
	 * approximate it conservatively as Top.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {
		return TopT.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Namespace> memberReadableNamespaces() {
		return Collections.<Namespace> singleton(dummyNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	public Namespace memberWriteableNamespace() {
		return dummyNamespace();
	}

	private Namespace dummyNamespace() {
		return new Namespace() {

			public Namespace getParentScope() {
				return null;
			}

			public String getName() {
				return null;
			}

			public SimpleNode getAst() {
				return null;
			}

			public Model model() {
				return null;
			}

			public Member lookupMember(String memberName) {
				return null;
			}

			public boolean isSystem() {
				return false;
			}

			public Map<String, Module> getModules() {
				return null;
			}

			public Module getGlobalNamespace() {
				return null;
			}

			public Map<String, Function> getFunctions() {
				return null;
			}

			public String getFullName() {
				return null;
			}

			public Map<String, Class> getClasses() {
				return null;
			}

			public Cfg getCfg() {
				return null;
			}

			public CodeObject codeObject() {
				return null;
			}

			public CodeBlock asCodeBlock() {
				return null;
			}

			public void addModule(Module module) {
			}

			public void addFunction(Function function) {
			}

			public void addClass(Class klass) {
			}
		};
	}

}
