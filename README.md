# akka-actr-benchmark
Benchmark [akka](https://github.com/akka/akka) and [actr](https://github.com/zakgof/actr) actor model implementations in Java

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
## benchmark 3: sleeping parallel run

Similarly to the previous benchmark, the Master actor sends 100k messages to each of 100 Runner actors, but this time each Runner sleeps for 20ms before responding.
This benchmark simulates massive concurrent operations without active CPU utilization, such as massive network operarions. We expect that JDK Project Loom's virtual threads should perform best here. 


## benchmark 4: parallel merge sort

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


A runner actor divides an array in two parts, then creates two new actors and sends them the halves.
Then actor waits until both children reply with sorted arrays, merges them into a single sorted array and sends back to the parent actor.
The test array had 2^20 random integers.

## frameworks compared

- akka 2.5.21 https://github.com/akka/akka
- actr 0.4.0 https://github.com/zakgof/actr

## how to run

```
gradlew clean jmh
```

## results
```
Intel Core i5-6500
OpenJDK 12
Windows 10

Benchmark            Framework   Score    Error   Units
Sequential run          akka    21.500  ± 0.484   s/op
Sequential run          actr     4.065  ± 0.129   s/op
Parallel run            akka     6.789  ± 0.622   s/op
Parallel run            actr     6.587  ± 0.602   s/op
Merge sort              akka    30.283  ± 1.091   s/op
Merge sort              actr     4.753  ± 0.139   s/op

(Smaller numbers are better).
```
## conclusion

Actr outperforms Akka in benchmarks involving massive creating of actors. The performance of message exchange looks comparable.
