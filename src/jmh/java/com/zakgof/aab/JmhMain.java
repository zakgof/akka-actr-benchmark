package com.zakgof.aab;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhMain {
	  public static void main(String[] args) throws RunnerException {

	        Options opt = new OptionsBuilder()
	                .include(JmhDive.class.getSimpleName())
	                .include(JmhMassiveTell.class.getSimpleName())
	                .include(JmhMergeSort.class.getSimpleName())
	                .forks(1)
	                .build();

	        new Runner(opt).run();
	    }
}
