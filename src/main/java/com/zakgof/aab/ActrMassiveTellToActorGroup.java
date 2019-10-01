package com.zakgof.aab;

import java.util.BitSet;
import java.util.function.Supplier;

import com.zakgof.actr.ActorRef;
import com.zakgof.actr.ActorSystem;
import com.zakgof.actr.Actr;
import com.zakgof.actr.FiberScheduler;
import com.zakgof.actr.IActorScheduler;

public class ActrMassiveTellToActorGroup {

	public static void main(String[] args) throws InterruptedException {
		System.err.println("ACTR Massive Tell started...");
		long start = System.currentTimeMillis();
		run(() -> new FiberScheduler(), 100000, 100);
		long end = System.currentTimeMillis();
		System.err.println("finished in " + (end - start));
	}

	public static void run(Supplier<IActorScheduler> schedulerFactory, int messagecount, int actorcount) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("actr-massive", schedulerFactory);
		ActorRef<Master> master = system.actorOf(() -> new Master(messagecount, actorcount));

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
			for (int a=0; a<actorcount; a++) {
				ActorRef<Runner> runner = Actr.system().actorOf(Runner::new);
				int aa = a;
				for (int m=0; m<messagecount; m++) {
					int mm = m;
					runner.tell(r -> r.run(new int[] {aa, mm}));
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
			Actr.<Master>caller().tell(m -> m.runnerReplied(msg));
		}
	}

}
