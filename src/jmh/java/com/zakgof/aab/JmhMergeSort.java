package com.zakgof.aab;

import com.zakgof.actr.Schedulers;
import java.util.Random;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class JmhMergeSort {

    public final int DATASIZE = 1 << 20;

    private Random random = new Random(0L);
    private int[] input = IntStream.range(0, DATASIZE).map(i -> random.nextInt()).toArray();

    @Param({ "akka", "actr" /*, "actr-sngl", "actr-fixd", "actr-loom"*/ })
    public String what;

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void run() throws InterruptedException {
        switch (what) {
            case "akka"         : AkkaMergeSort.sort(input); break;
            case "actr"         : ActrMergeSort.sort(input, Schedulers.newForkJoinPoolScheduler(10)); break;

            case "actr-sngl"     : ActrMergeSort.sort(input, Schedulers.newSingleThreadScheduler()); break;
            case "actr-fixd"     : ActrMergeSort.sort(input, Schedulers.newFixedThreadPoolScheduler(Runtime.getRuntime().availableProcessors(), 10)); break;
             // case "actr-loom"     -> ActrMergeSort.sort(input, Schedulers.newExecutorBasedScheduler(Executors.newUnboundedExecutor(Thread.builder().virtual().factory()), 10));
        }
    }
}
