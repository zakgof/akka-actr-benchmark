package com.zakgof.aab;

import java.util.BitSet;

import com.zakgof.actr.Actr;
import com.zakgof.actr.IActorRef;
import com.zakgof.actr.IActorScheduler;
import com.zakgof.actr.IActorSystem;
import com.zakgof.actr.Schedulers;

public class ActrMassiveTellToActorGroup {

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(10000);

        System.err.println("ACTR Massive Tell started...");
        long start = System.currentTimeMillis();
        run(300000, 100, Schedulers.newForkJoinPoolScheduler(10));
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    public static void run(int messagecount, int actorcount, IActorScheduler scheduler) throws InterruptedException {

        final IActorSystem system = Actr.newSystem("actr-massive", scheduler);
        IActorRef<Master> master = system.actorOf(() -> new Master(messagecount, actorcount));

        master.tell(m -> m.start());
        system.shutdownCompletable().join();
    }

    private static class Master {

        private final int messagecount;
        private final BitSet bitset;
        private final int actorcount;

        public Master(int messagecount, int actorcount) {
            this.messagecount = messagecount;
            this.actorcount = actorcount;
            this.bitset = new BitSet(messagecount * actorcount);
            bitset.set(0, messagecount * actorcount);
        }

        public void start() {
            for (int a = 0; a < actorcount; a++) {
                IActorRef<Runner> runner = Actr.system().actorOf(Runner::new);
                int aa = a;
                for (int m = 0; m < messagecount; m++) {
                    int mm = m;
                    runner.tell(r -> r.run(new int[] { aa, mm }));
                }
            }
        }

        public void runnerReplied(int[] msg) {
            int actorNo = msg[0];
            int messageNo = msg[1];
            bitset.clear(actorNo * messagecount + messageNo);
            if (bitset.isEmpty()) {
                Actr.system().shutdown();
            }
        }
    }

    private static class Runner {
        private void run(int[] msg) {
            Actr.<Master> caller().tell(m -> m.runnerReplied(msg));
        }
    }

}
