package uk.ac.ic.doc.gander.analysers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.MethodCallHelper;
import uk.ac.ic.doc.gander.analysis.MethodFinder;
import uk.ac.ic.doc.gander.analysis.signatures.SignatureBuilder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TImportable;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;

public class DominationLength extends HierarchyWalker {

	private Tallies stats = new Tallies();
	private MutableModel model;

	public DominationLength(Hierarchy hierarchy) throws ParseException,
			IOException {
		this.model = new MutableModel(hierarchy);
		this.walk(hierarchy);
	}

	public Tallies getResult() {
		return stats;
	}

	private void analyseClass(Class klass) {
		for (Function method : klass.getFunctions().values())
			analyseFunction(method);
	}

	private void analyseFunction(Function function) {
		SignatureBuilder chainAnalyser = new SignatureBuilder();

		TypeResolver typer = new TypeResolver(model);

		for (BasicBlock sub : function.getCfg().getBlocks()) {
			for (Call call : new MethodFinder(sub).calls()) {
				if (!isMethodCallOnName(call, function, typer))
					continue;

				Collection<Call> dependentCalls = chainAnalyser.signature(
						MethodCallHelper.extractMethodCallTarget(call), sub,
						function, typer);

				if (dependentCalls != null) {
					int count = countUniqueMethodNames(dependentCalls);
					// if (count > 1)
					// printChain(call, dependentCalls);

					stats.addTally(new Integer(count));
				}
				// If no dependent calls, this isn't a chain length of 0.
				// It means the call wasn't a method call at all! Something
				// like a function being called on a module rather than
				// an object method.
			}
		}
	}

	private void printChain(int count, Call call,
			Collection<Call> dependentCalls) {
		if (dependentCalls != null) {
			System.err.println("Chain length: " + count);
			System.err.println("'" + call + "' chain:\n" + dependentCalls
					+ "\n\n");
		}
	}

	private boolean isMethodCallOnName(Call call, Function function,
			TypeResolver typer) {
		if (!(call.func instanceof Attribute))
			return false;

		Attribute attr = (Attribute) call.func;
		if (!(attr.value instanceof Name))
			return false;

		Name variable = (Name) attr.value;

		return !(typer.typeOf(variable, function) instanceof TImportable);
	}

	private int countUniqueMethodNames(Iterable<Call> calls) {
		Set<String> methods = new HashSet<String>();
		for (Call call : calls) {
			methods.add(MethodCallHelper.extractMethodCallName(call).id);
		}
		return methods.size();
	}

	@Override
	protected void visitModule(
			uk.ac.ic.doc.gander.hierarchy.Module hierarchyModule) {
		if (hierarchyModule.isSystem())
			return;

		Module module;
		try {
			module = model.loadModule(hierarchyModule.getFullyQualifiedName());
		} catch (ParseException e) {
			System.err
					.println("MISSED DATA WARNING: error while parsing module"
							+ hierarchyModule.getFullyQualifiedName());
			System.err.println(e);
			return;
		} catch (IOException e) {
			System.err
					.println("MISSED DATA WARNING: error while finding module"
							+ hierarchyModule.getFullyQualifiedName());
			System.err.println(e);
			return;
		}

		for (Function function : module.getFunctions().values())
			analyseFunction(function);

		for (Class klass : module.getClasses().values())
			analyseClass(klass);
	}

	@Override
	protected void visitPackage(Package hierarchyPackage) {
		if (hierarchyPackage.isSystem())
			return;

		uk.ac.ic.doc.gander.model.Package pkg;
		try {
			pkg = model.loadPackage(hierarchyPackage.getFullyQualifiedName());
		} catch (ParseException e) {
			System.err
					.println("MISSED DATA WARNING: error while parsing package"
							+ hierarchyPackage.getFullyQualifiedName());
			System.err.println(e);
			return;
		} catch (IOException e) {
			System.err
					.println("MISSED DATA WARNING: error while finding package"
							+ hierarchyPackage.getFullyQualifiedName());
			System.err.println(e);
			return;
		}

		for (Function function : pkg.getFunctions().values())
			analyseFunction(function);

		for (Class klass : pkg.getClasses().values())
			analyseClass(klass);
	}
}
