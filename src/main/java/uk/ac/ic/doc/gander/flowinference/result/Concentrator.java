package uk.ac.ic.doc.gander.flowinference.result;

import java.util.Set;

public final class Concentrator<DI, DO> implements Result.Processor<DI> {

    public interface DatumProcessor<DI, DO> {
        Result<DO> process(DI datum);
    }

    private final RedundancyEliminator<DO> concentratedResult = new RedundancyEliminator<DO>();
    private final DatumProcessor<DI, DO> processor;
    private final Result<DO> top;

    public static <DI, DO> Concentrator<DI, DO> newInstance(
            DatumProcessor<DI, DO> processor, Result<DO> top) {
        return new Concentrator<DI, DO>(processor, top);
    }

    public Result<DO> result() {
        return concentratedResult.result();
    }

    public void processInfiniteResult() {
        concentratedResult.add(top);
    }

    public void processFiniteResult(Set<DI> result) {
        for (DI datum : result) {
            concentratedResult.add(processor.process(datum));
            if (concentratedResult.isFinished())
                break;
        }
    }
    
    private Concentrator(DatumProcessor<DI, DO> processor, Result<DO> top) {
        this.top = top;
        if (processor == null)
            throw new NullPointerException("DatumProcessor not optional");

        this.processor = processor;
    }


}


