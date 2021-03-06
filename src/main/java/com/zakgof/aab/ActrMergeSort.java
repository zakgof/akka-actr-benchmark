package com.zakgof.aab;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import com.zakgof.actr.Actr;
import com.zakgof.actr.IActorRef;
import com.zakgof.actr.IActorScheduler;
import com.zakgof.actr.IActorSystem;
import com.zakgof.actr.Schedulers;

public class ActrMergeSort {

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random(0L);
        int[] input = IntStream.range(0, 1 << 20).map(i -> random.nextInt()).toArray();
        System.err.println("ACTR merge sort started...");
        long start = System.currentTimeMillis();
        sort(input, Schedulers.newForkJoinPoolScheduler(10));
        // sort(input, Schedulers.newSingleThreadScheduler());
        // sort(input, Schedulers.newFixedThreadPoolScheduler(4, 10));
        // sort(input, Schedulers.newThreadPerActorScheduler());
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    public static void sort(int[] input, IActorScheduler scheduler) {
        final IActorSystem system = Actr.newSystem("actrsort", scheduler);
        final IActorRef<MasterActor> master = system.actorOf(MasterActor::new, "master");
        master.tell(m -> m.start(input));
        system.shutdownCompletable().join();
    }

    interface IResultReceiver {
        void result(int[] array, int side);
    }

    private static class MasterActor implements IResultReceiver {

        public void start(int[] array) {
            IActorRef<Sorter> sorter = Actr.system().actorOf(() -> new Sorter(-1), "c");
            sorter.tell(s -> s.run(array));
        }

        @Override
        public void result(int[] array, int side) {
            Actr.system().shutdown();
        }
    }

    private static class Sorter implements IResultReceiver {

        private final int side;
        private int[][] res = new int[2][];
        private IActorRef<IResultReceiver> upstream;

        public Sorter(int side) {
            this.side = side;
        }

        public void run(int[] array) {
            upstream = Actr.<IResultReceiver> caller();
            if (array.length == 1)
                upstream.tell(s -> s.result(array, side));
            else {

                int[] left = Arrays.copyOfRange(array, 0, array.length / 2);
                int[] right = Arrays.copyOfRange(array, array.length / 2, array.length);

                IActorRef<Sorter> a = Actr.system().actorOf(() -> new Sorter(0), "a");
                IActorRef<Sorter> b = Actr.system().actorOf(() -> new Sorter(1), "b");

                a.tell(s -> s.run(left));
                b.tell(s -> s.run(right));
            }
        }

        @Override
        public void result(int[] array, int fromside) {
            res[fromside] = array;
            if (res[0] != null && res[1] != null) {
                int[] resultarray = merge(res[0], res[1]);
                upstream.tell(s -> s.result(resultarray, side));
            }
        }

        public static int[] merge(int[] a, int[] b) {
            int[] answer = new int[a.length + b.length];
            int i = a.length - 1, j = b.length - 1, k = answer.length;
            while (k > 0)
                answer[--k] = (j < 0 || (i >= 0 && a[i] >= b[j])) ? a[i--] : b[j--];
            return answer;
        }

    }

}
