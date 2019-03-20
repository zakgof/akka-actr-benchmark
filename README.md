# akka-actr-benchmark
Benchmark akka and actr actor model implementations in Java

## benchmark 1: actors row

Each actor creates a new actor and sends it an incremented integer until 10000 actors are created.
Then a Finish message traverses the path back to the start

## benchmark 2: parallel merge sort implementation using actor model

A runner actor divides an array in two parts, then creates two new actors and sends them the halves.
Then actor waits until both children reply with sorted arrays, merges them into a single sorted array and sends back to the parent actor.

## frameworks compared:

- akka https://github.com/akka/akka
- actr https://github.com/zakgof/actr

## results
```
Benchmark         (what)  Mode  Cnt   Score   Error  Units
JmhDive.run         akka  avgt   25  22.839 ± 0.551   s/op
JmhDive.run         actr  avgt   25   4.039 ± 0.081   s/op
JmhMergeSort.run    akka  avgt   25  33.365 ± 0.846   s/op
JmhMergeSort.run    actr  avgt   25   3.317 ± 0.078   s/op

```
