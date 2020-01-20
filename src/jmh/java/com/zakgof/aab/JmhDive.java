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
public class JmhDive {

	public final int ACTORCOUNT = 1000000;

	@Param({ "akka", "actr-fork", "actr-fixd", "actr-sngl", /* "actr-loom" */ })
	public String what;

	@SuppressWarnings("preview")
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void run() throws InterruptedException {
		switch (what) {
			case "akka" -> AkkaDive.run(ACTORCOUNT);
			case "actr-fork" -> ActrDive.run(ACTORCOUNT, Schedulers.newForkJoinPoolScheduler(10));
			case "actr-fixd" -> ActrDive.run(ACTORCOUNT, Schedulers.newFixedThreadPoolScheduler(Runtime.getRuntime().availableProcessors(), 10));
			case "actr-sngl" -> ActrDive.run(ACTORCOUNT, Schedulers.newSingleThreadScheduler());
		  //  case "actr-loom" -> ActrDive.run(ACTORCOUNT, Schedulers.newExecutorBasedScheduler(Executors.newUnboundedExecutor(Thread.builder().virtual().factory()), 10));
		}
	}
}
