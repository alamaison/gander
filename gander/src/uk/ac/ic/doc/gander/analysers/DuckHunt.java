package uk.ac.ic.doc.gander.analysers;

import java.io.IOException;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.analysis.MethodFinder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.duckinference.DuckTyper;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Module;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelWalker;

public class DuckHunt {

	private Model model;
	private Tallies counts;
	private TypeResolver typer;

	public DuckHunt(Hierarchy hierarchy) throws Exception {
		this.model = new Model(hierarchy);
		this.counts = new Tallies();
		new HierarchyLoader().walk(hierarchy);
		this.typer = new TypeResolver(model);
		new ModelDucker().walk(model);
	}

	public Tallies getResult() {
		return counts;
	}

	private final class ModelDucker extends ModelWalker {

		@Override
		protected void visitFunction(Function function) {
			// only analyse methods within our target project's namespace
			if (function.isSystem())
				return;
				
			for (BasicBlock block : function.getCfg().getBlocks()) {
				for (Call call : new MethodFinder(block).calls()) {
					if (!isExternalMethodCallOnName(call, function))
						continue;

					countNumberOfTypesInferredFor(call, function, block);
				}
			}
		}
	}

	private void countNumberOfTypesInferredFor(Call call, Function function,
			BasicBlock block) {
		Set<Type> type = new DuckTyper(model).typeOf(call, block, function);
		counts.addTally(type.size());

		if (type.size() == 0) {
			System.out.println("unable to infer type from " + call + " in "
					+ function.getFullName());
		}
	}

	private boolean isMethodCallOnName(Call call, Function function) {
		if (!(call.func instanceof Attribute))
			return false;

		Attribute attr = (Attribute) call.func;
		if (!(attr.value instanceof Name))
			return false;

		Name variable = (Name) attr.value;

		// skip calls to module functions - they look like method calls but
		// we want to treat then differently

		return !(typer.typeOf(variable, function) instanceof uk.ac.ic.doc.gander.flowinference.types.TImportable);
	}

	private boolean isExternalMethodCallOnName(Call call, Function function) {
		if (!(call.func instanceof Attribute))
			return false;

		Attribute attr = (Attribute) call.func;
		if (!(attr.value instanceof Name))
			return false;

		Name variable = (Name) attr.value;

		// if function is a method of a class, skip calls to self (or
		// whatever the first parameter to a method
		// is called. We already know the type of these.
		if (function.getParentScope() instanceof uk.ac.ic.doc.gander.model.Class) {
			if (function.getFunctionDef().args.args.length > 0) {
				if (function.getFunctionDef().args.args[0] instanceof Name) {
					if (((Name) function.getFunctionDef().args.args[0]).id
							.equals(variable.id)) {
						return false;
					}

				} else {
					System.err.println("WARNING: method with "
							+ "non-name first parameter? "
							+ function.getFullName());

				}
			} else {
				System.err.println("WARNING: method with "
						+ "no object parameter? " + function.getFullName());
			}
		}

		// skip calls to module functions - they look like method calls but
		// we want to treat then differently

		return !(typer.typeOf(variable, function) instanceof uk.ac.ic.doc.gander.flowinference.types.TImportable);
	}

	/**
	 * Load every module and package module in the hierarchy.
	 */
	private final class HierarchyLoader extends HierarchyWalker {

		@Override
		protected void visitModule(Module module) {
			try {
				if (!module.isSystem())
					model.loadModule(module.getFullyQualifiedName());
			} catch (ParseException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "parsing module" + module.getFullyQualifiedName());
				System.err.println(e);
				return;
			} catch (IOException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "finding module" + module.getFullyQualifiedName());
				System.err.println(e);
			}
		}

		@Override
		protected void visitPackage(Package pkg) {
			try {
				if (!pkg.isSystem())
					model.loadPackage(pkg.getFullyQualifiedName());
			} catch (ParseException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "parsing package" + pkg.getFullyQualifiedName());
				System.err.println(e);
				return;
			} catch (IOException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "finding package" + pkg.getFullyQualifiedName());
				System.err.println(e);
				return;
			}
		}
	}
}
