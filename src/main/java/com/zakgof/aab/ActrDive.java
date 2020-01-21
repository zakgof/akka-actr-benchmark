package com.zakgof.aab;

import com.zakgof.actr.Actr;
import com.zakgof.actr.IActorRef;
import com.zakgof.actr.IActorScheduler;
import com.zakgof.actr.IActorSystem;
import com.zakgof.actr.Schedulers;

public class ActrDive {

    public static void main(String[] args) throws InterruptedException {
        System.err.println("ACTR Dive started...");
        long start = System.currentTimeMillis();
        run(1000000, Schedulers.newForkJoinPoolScheduler(10));
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    public static void run(int actorcount, IActorScheduler scheduler) throws InterruptedException {

        final IActorSystem system = Actr.newSystem("actr-dive", scheduler);
        final IActorRef<Master> master = system.actorOf(Master::new, "master");

        master.tell(m -> m.start(actorcount));
        system.shutdownCompletable().toCompletableFuture().join();
    }

    interface IFinisher {
        public void finish();
    }

    private static class Master implements IFinisher {

        public void start(int limit) {
            IActorRef<Runner> next = Actr.system().actorOf(() -> new Runner(limit));
            next.tell(r -> r.run(1));
        }

        @Override
        public void finish() {
            Actr.system().shutdown();
        }
    }

    private static class Runner implements IFinisher {

        private int limit;

        private Runner(int limit) {
            this.limit = limit;
        }

        private IActorRef<IFinisher> prev;

        private void run(int i) {
            this.prev = Actr.caller();
            if (i < limit) {
                IActorRef<Runner> next = Actr.system().actorOf(() -> new Runner(limit));
                next.tell(r -> r.run(i + 1));
            } else {
                prev.tell(IFinisher::finish);
            }
        }

        @Override
        public void finish() {
            prev.tell(IFinisher::finish);
        }
    }

}
