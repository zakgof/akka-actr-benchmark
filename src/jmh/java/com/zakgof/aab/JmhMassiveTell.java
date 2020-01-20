package com.zakgof.aab;

import java.util.concurrent.Executors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import com.zakgof.actr.Schedulers;

@State(Scope.Benchmark)
public class JmhMassiveTell {

	public final int ACTORCOUNT = 100;
	public final int MESSAGECOUNT = 100000;

	@Param({ "akka", "actr-fork", "actr-sngl", "actr-fixd", "actr-thrd", /* "actr-loom" */ })
	public String what;

	@SuppressWarnings("preview")
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void run() throws InterruptedException {
		switch (what) {
			case "akka" 		-> AkkaMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT);
			case "actr-fork" 	-> ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newForkJoinPoolScheduler(10));
			case "actr-sngl" 	-> ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newSingleThreadScheduler());
			case "actr-fixd" 	-> ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newFixedThreadPoolScheduler(Runtime.getRuntime().availableProcessors(), 10));
			case "actr-thrd" 	-> ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newThreadPerActorScheduler());
			// case "actr-loom" 	-> ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT, Schedulers.newExecutorBasedScheduler(Executors.newUnboundedExecutor(Thread.builder().virtual().factory()), 10));
		}
	}
}
