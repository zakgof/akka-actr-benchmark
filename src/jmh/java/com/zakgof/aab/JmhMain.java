package com.zakgof.aab;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class JmhMain {
	 public static void main(String[] args) throws RunnerException {

		 URLClassLoader classLoader = (URLClassLoader) JmhDive.class.getClassLoader();
		 StringBuilder classpath = new StringBuilder();
		 for(URL url : classLoader.getURLs())
		     classpath.append(url.getPath()).append(File.pathSeparator);
		 System.setProperty("java.class.path", classpath.toString());

	        Options opt = new OptionsBuilder()
	                .include(JmhDive.class.getSimpleName())
	                .include(JmhMassiveTell.class.getSimpleName())
	                .include(JmhMergeSort.class.getSimpleName())
	                .forks(1)
	                .warmupIterations(3)
	                .measurementIterations(7)
	                .resultFormat(ResultFormatType.TEXT)
	                .warmupTime(TimeValue.seconds(20))
	                .measurementTime(TimeValue.seconds(20))
	                .build();

	        new Runner(opt).run();
	    }
}
