package com.zakgof.aab;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import com.zakgof.actr.FiberScheduler;
import com.zakgof.actr.ForkJoinPoolScheduler;

@State(Scope.Benchmark)
public class JmhMassiveTell {

	public final int ACTORCOUNT = 100;
	public final int MESSAGECOUNT = 100000;

	@Param({ "akka", "actr" })
	public String what;

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void run() throws InterruptedException {
		switch (what) {
			case "akka":
				AkkaMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT);
				break;
			case "actr-forkjoin":
				ActrMassiveTellToActorGroup.run(() -> new ForkJoinPoolScheduler(10), MESSAGECOUNT, ACTORCOUNT);
				break;
			case "actr-fibers":
				ActrMassiveTellToActorGroup.run(() -> new FiberScheduler(), MESSAGECOUNT, ACTORCOUNT);
				break;
		}
	}
}
