package com.zakgof.aab;

import java.util.ArrayList;
import java.util.List;

import com.zakgof.actr.ActorRef;
import com.zakgof.actr.ActorSystem;
import com.zakgof.actr.Actr;

public class ActrRing {
	
	
	public static void main(String[] args) throws InterruptedException {
		run(10, 2000000);
	}
	
	public static void run(int ringsize, int rounds) throws InterruptedException {
		
		ActorSystem system = ActorSystem.create("actrring");
		final ActorRef<StartFinish> sf = system.actorOf(StartFinish::new, "master");
		final List<ActorRef<Runner>> runners = new ArrayList<>(ringsize);
		for (int i=0; i<ringsize; i++) {
			runners.add(system.actorOf(() -> new Runner(rounds), "r" + i));
		}
		for (int i=0; i<ringsize; i++) {
			ActorRef<Runner> next = runners.get((i + 1) % ringsize);
			runners.get(i).tell(r -> r.setActors(next, sf));
		}
		
		sf.tell(r -> r.start("1234567890", runners.get(0)));
				
		system.shutdownCompletable().join();
		
	}

	private static class Runner {
		private int count = 0;
		private ActorRef<Runner> next;
		private ActorRef<StartFinish> finish;
		private final int rounds;
		
		public Runner(int rounds) {
			this.rounds = rounds;
		}

		public void setActors(ActorRef<Runner> next, ActorRef<StartFinish> finish) {
			this.next = next;
			this.finish = finish;
		}
		
		public void send(final String token) {
			// System.err.println("     Runner send " + count + " to " + next + "  in  " + Thread.currentThread().getName());
			if (count < rounds) {
				count++;
				next.tell(r -> r.send(token));
			} else {
				finish.tell(f -> f.finish(token));
			}
		}
	}
	
	private static class StartFinish {
			
		// private long start;
		
		public void start(String token, ActorRef<Runner> starter) {
			// System.err.println("Started !!! ");
			// start = System.currentTimeMillis();
			starter.tell(r -> r.send(token));
		}
		public void finish(String token) {
			// long end = System.currentTimeMillis();
			// System.err.println("AKTR Finished !!! " + (end - start));
			Actr.current().system().shutdown();
		}
	}
	

}
