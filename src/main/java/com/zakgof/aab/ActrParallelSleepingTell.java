package com.zakgof.aab;

import com.zakgof.actr.Actr;
import com.zakgof.actr.IActorRef;
import com.zakgof.actr.IActorScheduler;
import com.zakgof.actr.IActorSystem;
import com.zakgof.actr.Schedulers;
import java.util.BitSet;

public class ActrParallelSleepingTell {

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(10000);

        System.err.println("ACTR Massive Tell started...");
        long start = System.currentTimeMillis();
        run(10000, Schedulers.newForkJoinPoolScheduler(10));
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    public static void run(int actorcount, IActorScheduler scheduler) throws InterruptedException {

        final IActorSystem system = Actr.newSystem("actr-massive", scheduler);
        IActorRef<Master> master = system.actorOf(() -> new Master(actorcount));

        master.tell(m -> m.start());
        system.shutdownCompletable().join();
    }

    private static class Master {

        private final BitSet bitset;
        private final int actorcount;

        public Master(int actorcount) {
            this.actorcount = actorcount;
            this.bitset = new BitSet(actorcount);
            bitset.set(0, actorcount);
        }

        public void start() {
            for (int a = 0; a < actorcount; a++) {
                IActorRef<Runner> runner = Actr.system().actorOf(Runner::new);
                int actoridx = a;
                runner.tell(r -> r.run(actoridx));
            }
        }

        public void runnerReplied(int actorIdx) {
            bitset.clear(actorIdx);
            if (bitset.isEmpty()) {
                Actr.system().shutdown();
            }
        }
    }

    private static class Runner {
        private static final int SLEEP_AMOUNT = 100;

        private void run(int actorIdx) {
            try {
                Thread.sleep(SLEEP_AMOUNT);
            } catch (InterruptedException e) {
            }
            Actr.<Master> caller().tell(m -> m.runnerReplied(actorIdx));
        }
    }

}
