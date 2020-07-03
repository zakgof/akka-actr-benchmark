package com.zakgof.aab;

import com.zakgof.actr.Schedulers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class JmhParallelTell{

    public final int ACTORCOUNT = 100;
    public final int MESSAGECOUNT = 100000;

    @Param({ "akka", "actr" /* , "actr-sngl", "actr-fixd", "actr-thrd", "actr-loom" */ })
    public String what;

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void run() throws InterruptedException {
        switch (what) {
            case "akka"          : AkkaMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT); break;
            case "actr"          : ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newForkJoinPoolScheduler(10));  break;

            case "actr-sngl"     : ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newSingleThreadScheduler());  break;
            case "actr-fixd"     : ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newFixedThreadPoolScheduler(Runtime.getRuntime().availableProcessors(), 10)); break;
            case "actr-thrd"     : ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newThreadPerActorScheduler()); break;
            // case "actr-loom"     -> ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newExecutorBasedScheduler(Executors.newUnboundedExecutor(Thread.builder().virtual().factory()), 10));
        }
    }
}
