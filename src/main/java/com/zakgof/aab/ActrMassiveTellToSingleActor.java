package com.zakgof.aab;

import java.util.BitSet;
import java.util.function.Supplier;

import com.zakgof.actr.ActorRef;
import com.zakgof.actr.ActorSystem;
import com.zakgof.actr.Actr;
import com.zakgof.actr.DedicatedThreadScheduler;
import com.zakgof.actr.FiberScheduler;
import com.zakgof.actr.ForkJoinPoolScheduler;
import com.zakgof.actr.IActorScheduler;

public class ActrMassiveTellToSingleActor {

	public static void main(String[] args) throws InterruptedException {
		go(() -> new FiberScheduler(), "Fibers");
		go(() -> new ForkJoinPoolScheduler(10), "ForkJoinPool");
		go(() -> new DedicatedThreadScheduler(), "Threads");
	}

	private static void go(Supplier<IActorScheduler> factory, String name) throws InterruptedException {
		System.err.println("ACTR Massive Tell started on " + name);
		long start = System.currentTimeMillis();
		run(factory, 100000);
		long end = System.currentTimeMillis();
		System.err.println("finished in " + (end - start));
	}

	public static void run(Supplier<IActorScheduler> schedulerFactory, int messagecount) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("actr-massive", schedulerFactory);
		ActorRef<Master> master = system.actorOf(() -> new Master(messagecount));

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
			ActorRef<Runner> runner = Actr.system().actorOf(Runner::new);
			for (int i=0; i<limit; i++) {
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
			Actr.<Master>caller().tell(m -> m.runnerReplied(i));
		}
	}

}
