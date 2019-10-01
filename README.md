# akka-actr-benchmark
Benchmark [akka](https://github.com/akka/akka) and [actr](https://github.com/zakgof/actr) actor model implementations in Java

## Introducing Fiber Schedulers

 Oracle released an early access build of JDK with Project Loom featuring delimited continuations, fibers and tail-call elimination: https://jdk.java.net/loom/
 
 Fibers are light-weight user-mode execution threads, implemented by JVM rather than by the underlying OS (in fact, they are implemented on top of Continuations).
 
 "Light-weight" means that creating millions of fiber would be OK for a Java application and fiber creation is much faster than creating a native thread. This makes fibers an excellent base for actor implemenation. This is an attempt to implement a FiberScheduler: every actor here runs in a dedicated fiber.  

The Project Loom Team notes:
````
    These builds are not suitable for doing performance testing at this time. There are many significant changes going on that impact the performance and it will likely vary wildly from build to build as the implementation evolves._
````
but anyway I'm curious what is the current state of performance in these Early builds, so I'm running this benchmark with default Actr's ForkJoinPool based scheduler vs Loom Fiber based scheduler.


## how to run

 - download and unpack an Early Access JDK-14 build with Loom from https://jdk.java.net/loom/
 
 - setup gradle to pick that jdk, in `gradle.properties`:

```` 
   org.gradle.java.home=c:/оk-14-loom
````

 - clone the `loom-ea-2019-07-25` branch of both `actr` and `akka-actr-benchmark` into the same parent folder:

````
   git clone --branch loom-ea-2019-07-25 http://github.com/zakgof/actr
   git clone --branch loom-ea-2019-07-25 http://github.com/zakgof/akka-actr-benchmark
````

 - cd to `akka-actr-benchmark` dir and run the benchmark:
    
```` 
   gradlew jmh
````


## benchmark 1: sequential run

Each actor creates a new actor and sends it an incremented integer until 1 million actors are created.
Then a Finish message traverses the path back to the start

```
 Master -------0-------> Runner -----1------> Runner ------2------> ...  ------N------> Runner
        <---Finish------        <---Finish---        <---Finish---       <---Finish----
```


## benchmark 2: parallel run

Master actor sends 100k messages to each of 100 Runner actors. Each runner replies back to Master; once Master collects all the replies, the benchmark terminates.

```
             ------>   Runner
             <------
    Master   ------>   Runner
             <------
             ------>   Runner
             <------
             . . . 
```

## benchmark 3: parallel merge sort

A runner actor divides an array in two parts, then creates two new actors and sends them the halves.
Then actor waits until both children reply with sorted arrays, merges them into a single sorted array and sends back to the parent actor.
The test array had 2^20 random integers.

```
      Top-down step(fork)                         |
                                      [8, 5, 3, 2, 4, 6, 1, 7]
                                                  |
                                                  V
                                               Master
                                                  |
                                      [8, 5, 3, 2, 4, 6, 1, 7]
                                                  |
                                                  V
                                                Sorter
                                             /           \
                                    [8, 5, 3, 2]        [4, 6, 1, 7]              
                                        /                       \  
                                    Sorter                    Sorter     
                                   / \                         /     \  
                              [8,5]  [3,2]                 [4,6]     [1,7]
                               /        \                   /            \
                         Sorter         Sorter         Sorter           Sorter
                           /  \          / \             / \             /  \
                        [8]    [5]    [3]  [2]        [4]   [6]       [1]   [7] 
                        /        \    /       \       /       \       /        \
                      Sorter  Sorter Sorter  Sorter Sorter  Sorter  Sorter    Sorter 
                      
                      
                              
  Bottom-up step (join)               [1, 2, 3, 4, 5, 6, 7, 8]
                                                  ^
                                                  |
                                               Master
                                                  |
                                      [1, 2, 3, 4, 5, 6, 7, 8]
                                                  ^
                                                  |
                                                Sorter
                                             /           \
                                    [2, 3, 5, 8]        [1, 4, 6, 7]              
                                        /                       \  
                                    Sorter                    Sorter     
                                   / \                         /     \  
                              [5,8]  [2,3]                 [4,6]     [1,7]
                               /        \                   /            \
                         Sorter         Sorter         Sorter           Sorter
                           /  \          / \             / \             /  \
                        [8]    [5]    [3]  [2]        [4]   [6]       [1]   [7] 
                        /        \    /       \       /       \       /        \
                      Sorter  Sorter Sorter  Sorter Sorter  Sorter  Sorter    Sorter 
```


## frameworks compared:

- akka 2.5.21 https://github.com/akka/akka
- actr 0.2.0 https://github.com/zakgof/actr

## results
```
Intel Core i5-6500
OpenJDK 12+33

Benchmark            Framework  Mode  Cnt   Score    Error   Units
Sequential run          akka    avgt   25   23.333 Â± 0.656   s/op
Sequential run          actr    avgt   25    5.883 Â± 0.151   s/op
Parallel run            akka    avgt   25    6.555 Â± 0.422   s/op
Parallel run            actr    avgt   25    4.904 Â± 0.532   s/op
Merge sort              akka    avgt   25   34.064 Â± 1.492   s/op
Merge sort              actr    avgt   25    7.921 Â± 0.082   s/op

Smaller numbers are better.
```
## conclusion

Actr outperforms Akka in all three benchmarks on default settings. The difference is especially significant in benchmarks involving creating actors.
