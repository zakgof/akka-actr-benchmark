# akka-actr-benchmark
Benchmark akka and actr actor model implementations in Java

## benchmark 1: actors row

Each actor creates a new actor and sends it an incremented integer until 1 million actors are created.
Then a Finish message traverses the path back to the start

```
 Master -------0-------> Runner -----1------> Runner ------2------> ...  ------N------> Runner
        <---Finish------        <---Finish---        <---Finish---       <---Finish----
```

## benchmark 2: parallel merge sort implementation using actor model

A runner actor divides an array in two parts, then creates two new actors and sends them the halves.
Then actor waits until both children reply with sorted arrays, merges them into a single sorted array and sends back to the parent actor.

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

- akka https://github.com/akka/akka
- actr https://github.com/zakgof/actr

## results
```
Intel Core i5-6500
OpenJDK 12+33
akka 2.5.21
actr 0.0.5

Benchmark         (what)  Mode  Cnt   Score   Error  Units
JmhDive.run         akka  avgt   25  22.839 ± 0.551   s/op
JmhDive.run         actr  avgt   25   4.039 ± 0.081   s/op
JmhMergeSort.run    akka  avgt   25  33.365 ± 0.846   s/op
JmhMergeSort.run    actr  avgt   25   3.317 ± 0.078   s/op

```
