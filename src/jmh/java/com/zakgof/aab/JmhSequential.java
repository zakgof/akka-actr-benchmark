package com.zakgof.aab;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import com.zakgof.actr.Schedulers;

@State(Scope.Benchmark)
public class JmhSequential {

	public final int ACTORCOUNT = 1000000;

	@Param({ "akka", "actr" /*, "actr-fixd", "actr-sngl", "actr-loom" */ })
	public String what;

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void run() throws InterruptedException {
		switch (what) {
			case "akka": AkkaSequential.run(ACTORCOUNT); break;
			case "actr": ActrSequential.run(ACTORCOUNT, Schedulers.newForkJoinPoolScheduler(10)); break;

			case "actr-fixd": ActrSequential.run(ACTORCOUNT, Schedulers.newFixedThreadPoolScheduler(Runtime.getRuntime().availableProcessors(), 10));  break;
			case "actr-sngl": ActrSequential.run(ACTORCOUNT, Schedulers.newSingleThreadScheduler()); break;
		  //  case "actr-loom" -> ActrSequential.run(ACTORCOUNT, Schedulers.newExecutorBasedScheduler(Executors.newUnboundedExecutor(Thread.builder().virtual().factory()), 10));
		}
	}
}
