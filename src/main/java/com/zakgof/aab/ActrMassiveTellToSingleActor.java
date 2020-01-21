package com.zakgof.aab;

import java.util.BitSet;

import com.zakgof.actr.Actr;
import com.zakgof.actr.IActorRef;
import com.zakgof.actr.IActorSystem;

public class ActrMassiveTellToSingleActor {

    public static void main(String[] args) throws InterruptedException {
        System.err.println("ACTR Massive Tell started...");
        long start = System.currentTimeMillis();
        run(100000);
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    public static void run(int messagecount) throws InterruptedException {

        final IActorSystem system = Actr.newSystem("actr-massive");
        IActorRef<Master> master = system.actorOf(() -> new Master(messagecount));

        master.tell(m -> m.start());
        system.shutdownCompletable().join();
    }

    private static class Master {

        private final int limit;
        private final BitSet bitset;

        public Master(int limit) {
            this.limit = limit;
            this.bitset = new BitSet(limit);
            bitset.set(0, limit);
        }

        public void start() {
            IActorRef<Runner> runner = Actr.system().actorOf(Runner::new);
            for (int i = 0; i < limit; i++) {
                int i2 = i;
                runner.tell(r -> r.run(i2));
            }
        }

        public void runnerReplied(int i) {
            bitset.clear(i);
            if (bitset.isEmpty()) {
                Actr.system().shutdown();
            }
        }
    }

    private static class Runner {
        private void run(int i) {
            Actr.<Master> caller().tell(m -> m.runnerReplied(i));
        }
    }

}
