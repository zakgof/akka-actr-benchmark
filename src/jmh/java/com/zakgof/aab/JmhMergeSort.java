package com.zakgof.aab;

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

	public final int DATASIZE = 1 << 18;
	
	private Random random = new Random(0L);
	private int[] input = IntStream.range(0, DATASIZE).map(i -> random.nextInt()).toArray();

	@Param({ "akka", "actr" })
	public String what;

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void run() throws InterruptedException {
		switch (what) {
			case "akka": 
				AkkaMergeSort.sort(input);
				break;
			case "actr": 
				ActrMergeSort.sort(input);
				break;
		}
	}
}
