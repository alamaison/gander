package uk.ac.ic.doc.gander.analysers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.analysis.MethodFinder;
import uk.ac.ic.doc.gander.calls.CallSite;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.duckinference.DuckTyper;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelWalker;
import uk.ac.ic.doc.gander.model.MutableModel;

public class CallTargetTypes {

	private final MutableModel model;
	private final TypeResolver typer;
	private final Map<CallSite, Set<Type>> types = new HashMap<CallSite, Set<Type>>();

	public CallTargetTypes(Hierarchy hierarchy) throws Exception {
		System.out.println("Creating model from hierarchy");
		this.model = new DefaultModel(hierarchy);
		System.out.println("Loading all non-system modules in hierarchy");
		new HierarchyLoader().walk(hierarchy);
		System.out.println("Performing flow-based type inference");
		this.typer = new TypeResolver(new ZeroCfaTypeEngine());
		System.out.println("Running signature analysis");
		new ModelDucker().walk(model);
	}

	public Map<CallSite, Set<Type>> getResult() {
		return types;
	}

	private final class ModelDucker extends ModelWalker {

		@Override
		protected void visitFunction(Function function) {
			// only analyse methods within our target project's namespace
			if (function.isSystem())
				return;

			for (BasicBlock block : function.getCfg().getBlocks()) {
				for (Call call : new MethodFinder(block).calls()) {

					// if function is a method of a class, skip calls to self
					// (or whatever the first parameter to a method
					// is called. We already know the type of these.
					if (!CallHelper.isExternalMethodCallOnName(call, function,
							typer))
						continue;

					Set<Type> type = new DuckTyper(model, typer).typeOf(call,
							block, function);
					types.put(new CallSite(call, function, block), type);
				}
			}
		}
	}

	/**
	 * Load every module and package module in the hierarchy.
	 */
	private final class HierarchyLoader extends HierarchyWalker {

		@Override
		protected void visitSourceFile(SourceFile module) {
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
