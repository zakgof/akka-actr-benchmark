package com.zakgof.aab;

import java.util.ArrayList;
import java.util.List;

import com.zakgof.actr.ActorRef;
import com.zakgof.actr.ActorSystem;

public class ActrRing {
	
	public static void main(String[] args) throws InterruptedException {
		
		final ActorRef<StartFinish> sf = ActorSystem.dflt().actorOf(StartFinish::new, "master");
		final List<ActorRef<Runner>> runners = new ArrayList<>(10);
		for (int i=0; i<10; i++) {
			runners.add(ActorSystem.dflt().actorOf(Runner::new, "r" + i));
		}
		for (int i=0; i<10; i++) {
			ActorRef<Runner> next = runners.get((i + 1) % 10);
			runners.get(i).tell(r -> r.setActors(next, sf));
		}
		
		sf.tell(r -> r.start("1234567890", runners.get(0)));
		
		Thread.sleep(60000);
		// ActorSystem.dflt().shutdown();
		
	}

	private static class Runner {
		private int count = 0;
		private ActorRef<Runner> next;
		private ActorRef<StartFinish> finish;
		
		public void setActors(ActorRef<Runner> next, ActorRef<StartFinish> finish) {
			this.next = next;
			this.finish = finish;
		}
		
		public void send(final String token) {
			// System.err.println("     Runner send " + count + " to " + next + "  in  " + Thread.currentThread().getName());
			if (count < 1000000) {
				count++;
				next.tell(r -> r.send(token));
			} else {
				finish.tell(f -> f.finish(token));
			}
		}
	}
	
	private static class StartFinish {
			
		private long start;
		
		public void start(String token, ActorRef<Runner> starter) {
			System.err.println("Started !!! ");
			start = System.currentTimeMillis();
			starter.tell(r -> r.send(token));
			
		}
		public void finish(String token) {
			long end = System.currentTimeMillis();
			System.err.println("AKTR Finished !!! " + (end - start));
			System.exit(0);
		}
	}
	

}
