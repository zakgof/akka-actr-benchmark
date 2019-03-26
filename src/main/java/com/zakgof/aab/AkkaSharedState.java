package com.zakgof.aab;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class AkkaSharedState {

	private static final int ACTORS = 10000000;
	
	public static void main(String[] args) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("akka-shared");
		final ActorRef runner = system.actorOf(Runner.props(), "runner");
		
		for (int i=0; i<ACTORS; i++) {
			runner.tell(i, ActorRef.noSender());
		}
		system.getWhenTerminated().toCompletableFuture().join();
	}

	private static class Runner extends AbstractActor {
		
		int counter = 0;

		static public Props props() {
			return Props.create(Runner.class, Runner::new);
		}

		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(Integer.class, v -> run(v))
				.build();
		}
		
		private void run(int v) {
			counter++;
			if (counter > ACTORS)
				System.err.println("OVERFLOW");
			if (counter == ACTORS) {
				System.err.println("Reached goal");
			}
		}
	}

}
