package com.zakgof.aab;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class JmhRing {

	@Param({ "5", "10", "20" })
	public int size;
	
	public int rounds = 1000000;

	@Param({ "akka", "actr" })
	public String what;

	@Benchmark
	public void run() throws InterruptedException {
		switch (what) {
			case "akka": 
				AkkaRing.run(size, rounds);
				break;
			case "actr": 
				ActrRing.run(size, rounds);
				break;
		}
	}
}
