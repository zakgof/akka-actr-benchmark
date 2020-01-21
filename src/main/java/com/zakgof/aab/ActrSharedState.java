package com.zakgof.aab;

import com.zakgof.actr.Actr;
import com.zakgof.actr.IActorRef;
import com.zakgof.actr.IActorSystem;

public class ActrSharedState {

    private static final int ACTORS = 10000000;

    public static void main(String[] args) throws InterruptedException {

        final IActorSystem system = Actr.newSystem("akka-shared");
        final IActorRef<Runner> runner = system.actorOf(Runner::new, "runner");

        System.err.println("GO");

        for (int i = 0; i < ACTORS; i++) {
            final int ii = i;
            runner.tell(r -> r.run(ii));
        }
        system.shutdownCompletable().join();
    }

    private static class Runner {
        int counter = 0;

        public void run(final int v) {
            counter++;
            if (counter > ACTORS)
                System.err.println("OVERFLOW " + counter + " " + Thread.currentThread().getName());
            if (counter == ACTORS) {
                System.err.println("Reached goal");
            }

        }
    }

}
