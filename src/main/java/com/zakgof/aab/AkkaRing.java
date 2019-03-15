package com.zakgof.aab;

import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;


public class AkkaRing {
	
	public static void main(String[] args) throws InterruptedException {
		
		final ActorSystem system = ActorSystem.create("helloakka");
		
		final ActorRef sf = system.actorOf(StartFinish.props(), "startFinish");
		
		final List<ActorRef> runners = new ArrayList<>(10);
		for (int i=0; i<10; i++) {
			runners.add(system.actorOf(Runner.props(sf), "runner-" + i));
		}
		for (int i=0; i<10; i++) {
			ActorRef next = runners.get((i+1)%10);
			runners.get(i).tell(next, ActorRef.noSender());
		}
		
		sf.tell(runners.get(0), ActorRef.noSender());
		sf.tell("1234567890", ActorRef.noSender());
		
		Thread.sleep(60000);
		// ActorSystem.dflt().shutdown();
		
	}

	private static class Runner extends AbstractActor {
		private int count = 0;
		private ActorRef next;
		private ActorRef finish;
		
		public Runner(ActorRef finish) {
			this.finish = finish;
		}

		static public Props props(ActorRef finish) {
			return Props.create(Runner.class, () -> new Runner(finish));
		}
		
		@Override
		public Receive createReceive() {
			return receiveBuilder().match(String.class, token -> {
				if (count < 1000000) {
					count++;
					next.tell(token, getSelf());
				} else {
					finish.tell(new Finish(), getSelf());
				}
				
			})	
			.match(ActorRef.class, next -> {
				this.next = next;
			})
			.build();
		}
	}
	
	private static class Finish {
	}
	
	private static class StartFinish extends AbstractActor {
		
		static public Props props() {
			return Props.create(StartFinish.class, () -> new StartFinish());
		}
				
		private ActorRef starter;
		private long start;
	
			@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(String.class, token -> {
					System.err.println("Started !!! ");
					start = System.currentTimeMillis();
					starter.tell(token, getSelf());
				}).match(ActorRef.class, starter -> {
					this.starter = starter;
				})
				.match(Finish.class, finish -> {
					long end = System.currentTimeMillis();
					System.err.println("AKKA Finished !!! " + (end - start));
					System.exit(0);
				})
				.build();
		}
	}
	

}
