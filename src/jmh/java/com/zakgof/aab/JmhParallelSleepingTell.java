package com.zakgof.aab;

import com.zakgof.actr.Schedulers;
import java.util.concurrent.Executors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class JmhParallelSleepingTell{

    public final int ACTORCOUNT = 1000;

    @Param({"actr-fjp", "actr-sngl", "actr-fixd", "actr-thrd", "actr-loom"})
    public String what;

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void run() throws InterruptedException {
        switch (what) {
            case "actr-fjp"      : ActrParallelSleepingTell.run(ACTORCOUNT, Schedulers.newForkJoinPoolScheduler(10));  break;
            case "actr-sngl"     : ActrParallelSleepingTell.run(ACTORCOUNT, Schedulers.newSingleThreadScheduler());  break;
            case "actr-fixd"     : ActrParallelSleepingTell.run(ACTORCOUNT, Schedulers.newFixedThreadPoolScheduler(Runtime.getRuntime().availableProcessors(), 10)); break;
            case "actr-thrd"     : ActrParallelSleepingTell.run(ACTORCOUNT, Schedulers.newThreadPerActorScheduler()); break;
            case "actr-loom"     : ActrParallelSleepingTell.run(ACTORCOUNT, Schedulers.newExecutorBasedScheduler(Executors.newVirtualThreadExecutor(), 10)); break;
        }
    }
}
