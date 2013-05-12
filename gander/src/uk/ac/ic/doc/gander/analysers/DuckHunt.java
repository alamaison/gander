package uk.ac.ic.doc.gander.analysers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.CallHelper;
import uk.ac.ic.doc.gander.analysis.MethodFinder;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.duckinference.DuckTyper;
import uk.ac.ic.doc.gander.ducktype.InterfaceRecovery;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.interfacetype.Feature;
import uk.ac.ic.doc.gander.interfacetype.InterfaceType;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelWalker;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.OldNamespace;

public class DuckHunt {

	private final MutableModel model;
	private final Tallies counts;
	private final TypeResolver typer;

	public DuckHunt(Hierarchy hierarchy) throws Exception {
		System.out.println("Creating model from hierarchy");
		this.model = new DefaultModel(hierarchy);
		this.counts = new Tallies();
		System.out.println("Loading all non-system modules in hierarchy");
		new HierarchyLoader().walk(hierarchy);
		System.out.println("Performing flow-based type inference");
		this.typer = new TypeResolver(new ZeroCfaTypeEngine());
		System.out.println("Running signature analysis");
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

					// if function is a method of a class, skip calls to self
					// (or whatever the first parameter to a method
					// is called. We already know the type of these.
					if (!CallHelper.isExternalMethodCallOnName(call, function,
							typer))
						continue;

					countNumberOfTypesInferredFor(call, function, block);
				}
			}
		}
	}

	private void countNumberOfTypesInferredFor(Call call, Function function,
			BasicBlock block) {
		Result<Type> type = new DuckTyper(model, typer).typeOf(call, block,
				function);

		int size = type.transformResult(new Transformer<Type, Integer>() {

			@Override
			public Integer transformFiniteResult(Set<Type> result) {
				return result.size();
			}

			@Override
			public Integer transformInfiniteResult() {
				throw new AssertionError("This code wasn't "
						+ "written to cope with an "
						+ "infinite type.  Update it.");
			}
		});

		counts.addTally(size);

		if (size == 0) {
			System.out.println("unable to infer type from " + call + " in "
					+ function.getFullName() + "(signature "
					+ calculateDependentMethodNames(call, block, function)
					+ ")");
		}

		if (size > 80) {
			System.out.println("large number (" + size
					+ ") of types inferred from " + call + " in "
					+ function.getFullName() + " \n\t(signature "
					+ calculateDependentMethodNames(call, block, function)
					+ ")");
		}
	}

	private Set<String> calculateDependentMethodNames(Call call,
			BasicBlock containingBlock, OldNamespace scope) {

		InterfaceType recoveredInterface = new InterfaceRecovery(typer)
				.inferDuckType(call, containingBlock, scope, false);

		Set<String> methods = new HashSet<String>();
		for (Feature feature : recoveredInterface) {
			if (feature instanceof NamedMethodFeature) {
				methods.add(((NamedMethodFeature) feature).name());
			}
		}
		return methods;
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
