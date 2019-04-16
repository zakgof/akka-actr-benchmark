# akka-actr-benchmark
Benchmark akka and actr actor model implementations in Java

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

## benchmark 3: parallel merge sort implementation using actor model

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
Sequential run          akka    avgt   25   23.333 ± 0.656   s/op
Sequential run          actr    avgt   25    5.883 ± 0.151   s/op
Parallel run            akka    avgt   25    6.555 ± 0.422   s/op
Parallel run            actr    avgt   25    4.904 ± 0.532   s/op
Merge sort              akka    avgt   25   34.064 ± 1.492   s/op
Merge sort              actr    avgt   25    7.921 ± 0.082   s/op


```
