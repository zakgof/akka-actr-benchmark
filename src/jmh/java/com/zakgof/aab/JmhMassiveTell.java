package com.zakgof.aab;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

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
			case "actr": 
				ActrMassiveTellToActorGroup.run(MESSAGECOUNT, ACTORCOUNT);
				break;
		}
	}
}
