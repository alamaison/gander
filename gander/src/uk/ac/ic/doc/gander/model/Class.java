package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorIF;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectDefinitionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.flowgoals.InstanceCreationPosition;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock;
import uk.ac.ic.doc.gander.model.codeblock.DefaultCodeBlock.Acceptor;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.name_binding.InScopeVariableFinder;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class Class implements Namespace {

	private final Map<String, Function> methods = new HashMap<String, Function>();
	private final Map<String, Class> classes = new HashMap<String, Class>();

	private final ClassCO codeObject;
	private final ClassDef cls;
	private final Namespace parent;
	private CodeBlock codeBlock = null;
	private final Model model;

	public Class(ClassDef cls, Namespace parent, Model model) {
		this.cls = cls;
		this.parent = parent;
		this.model = model;
		this.codeObject = new ClassCO(this, parent.codeObject());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * A class's execution namespace is accessible by attribute access on any
	 * expression that the class code object can reach as well as any expression
	 * that can result in an instance of the class.
	 * 
	 * TODO: The accessibility is different depending on whether the attribute
	 * is being read or written. Class namespaces members are readable via
	 * instances but not writable via them.
	 */
	public Result<ModelSite<? extends exprType>> references(
			SubgoalManager goalManager) {

		RedundancyEliminator<ModelSite<? extends exprType>> references = new RedundancyEliminator<ModelSite<? extends exprType>>();

		references.add(goalManager.registerSubgoal(new FlowGoal(
				new CodeObjectDefinitionPosition(codeObject))));
		references.add(goalManager.registerSubgoal(new FlowGoal(
				new InstanceCreationPosition(codeObject))));

		return references.result();
	}

	public Set<Variable> variablesInScope(String name) {
		return new InScopeVariableFinder(codeObject, name).variables();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Legally, only global names (i.e. ones whose binding location is their
	 * containing module) can be written to inside a nested code object so this
	 * will return at most one variable.
	 */
	public Set<Variable> variablesWriteableInScope(String name) {

		Variable localVariable = new Variable(name, codeObject);

		if (localVariable.bindingLocation().codeObject().equals(codeObject)) {
			return Collections.singleton(localVariable);
		} else {
			return Collections.emptySet();
		}
	}

	public exprType[] inheritsFrom() {
		return cls.bases;
	}

	public String getName() {
		return ((NameTok) (cls.name)).id;
	}

	public String getFullName() {
		String parentName = parent.getFullName();
		if (parentName.isEmpty())
			return getName();
		else
			return parentName + "." + getName();
	}

	public Namespace getParentScope() {
		return parent;
	}

	public Map<String, Module> getModules() {
		return Collections.emptyMap();
	}

	public Map<String, Class> getClasses() {
		return classes;
		// return Collections.unmodifiableMap(classes);
	}

	public Map<String, Function> getFunctions() {
		return methods;
		// return Collections.unmodifiableMap(methods);
	}

	public void addModule(Module module) {
		throw new Error("A class cannot contain a package");
	}

	public void addClass(Class klass) {
		classes.put(klass.getName(), klass);
	}

	public void addFunction(Function function) {
		methods.put(function.getName(), function);
	}

	@Override
	public String toString() {
		return "Class[" + getFullName() + "]";
	}

	/**
	 * Classes inherit their systemness from their parent.
	 * 
	 * It isn't possible for a class to be system if it's containing module
	 * isn't a system module. In other words, the resolution of systemness is at
	 * the module level and all namespaces below that, inherit from their
	 * parent.
	 * 
	 * XXX: Another way to look at this is that systemness is a property of the
	 * associated <b>hierarchy</b> element so perhaps we should link model
	 * element to their hierarchy parent. However, some model elements don't
	 * have a hierarchy element. For example the dummy_builtin module.
	 */
	public boolean isSystem() {
		return parent.isSystem();
	}

	public ClassDef getAst() {
		return cls;
	}

	public Cfg getCfg() {
		throw new Error("Not implemented yet");
	}

	public Member lookupMember(String memberName) {
		if (classes.containsKey(memberName))
			return classes.get(memberName);
		else if (methods.containsKey(memberName))
			return methods.get(memberName);

		return null;
	}

	public CodeBlock asCodeBlock() {
		if (codeBlock == null) {
			// Classes don't have parameters that get bound after
			// declaration
			//
			// XXX: WTF? The ClassDef node has parameters! Are these from
			// the constructor?
			List<ModelSite<exprType>> args = Collections.emptyList();

			Acceptor acceptor = new Acceptor() {

				public void accept(VisitorIF visitor) throws Exception {
					for (stmtType stmt : cls.body) {
						stmt.accept(visitor);
					}
				}
			};

			codeBlock = new DefaultCodeBlock(args, acceptor);
		}

		return codeBlock;
	}

	public Module getGlobalNamespace() {
		return getParentScope().getGlobalNamespace();
	}

	public Model model() {
		return model;
	}

	public ClassCO codeObject() {
		return codeObject;
	}
}
