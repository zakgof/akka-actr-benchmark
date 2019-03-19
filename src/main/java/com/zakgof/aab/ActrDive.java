package com.zakgof.aab;

import com.zakgof.actr.ActorRef;
import com.zakgof.actr.ActorSystem;
import com.zakgof.actr.Actr;

public class ActrDive {

	public static void main(String[] args) throws InterruptedException {
		System.err.println("ACTR Dive started...");
		long start = System.currentTimeMillis();
		run(100000);
		long end = System.currentTimeMillis();
		System.err.println("finished in " + (end - start));
	}

	public static void run(int actorcount) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("actr-dive");
		final ActorRef<Master> master = system.actorOf(Master::new, "master");

		master.tell(m -> m.start(actorcount));
		system.shutdownCompletable().toCompletableFuture().join();
	}
	
	interface IFinisher {
		public void finish();
	}

	private static class Master implements IFinisher {

		public void start(int limit) {
			ActorRef<Runner> next = Actr.system().actorOf(() -> new Runner(limit));
			next.tell(r -> r.run(1));
		}
		
		public void finish() {
			Actr.system().shutdown();
		}
	}
	
	private static class Runner implements IFinisher {

		private int limit;
		
		private Runner(int limit) {
			this.limit = limit;
		}

		private ActorRef<IFinisher> prev;
		
		private void run(int i) {
			this.prev = Actr.caller();
			if (i < limit) {
				ActorRef<Runner> next = Actr.system().actorOf(() -> new Runner(limit));
				next.tell(r -> r.run(i+1));
			} else {
				prev.tell(IFinisher::finish);
			}
		}
		
		public void finish() {
			prev.tell(IFinisher::finish);
		}
	}

}