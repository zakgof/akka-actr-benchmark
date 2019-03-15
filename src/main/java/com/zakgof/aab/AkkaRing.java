package com.zakgof.aab;

import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class AkkaRing {

	public static void main(String[] args) throws InterruptedException {
		run(10, 2000000);
	}

	public static void run(int ringsize, int rounds) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("akkaring");

		final ActorRef sf = system.actorOf(StartFinish.props(), "master");

		final List<ActorRef> runners = new ArrayList<>(ringsize);
		for (int i = 0; i < ringsize; i++) {
			runners.add(system.actorOf(Runner.props(sf, rounds), "r" + i));
		}
		for (int i = 0; i < ringsize; i++) {
			ActorRef next = runners.get((i + 1) % ringsize);
			runners.get(i).tell(next, ActorRef.noSender());
		}

		sf.tell(runners.get(0), ActorRef.noSender());
		sf.tell("1234567890", ActorRef.noSender());

		system.getWhenTerminated().toCompletableFuture().join();

	}

	private static class Runner extends AbstractActor {
		private int count = 0;
		private ActorRef next;
		private ActorRef finish;
		private final int rounds;

		public Runner(ActorRef finish, int rounds) {
			this.finish = finish;
			this.rounds = rounds;
		}

		static public Props props(ActorRef finish, int rounds) {
			return Props.create(Runner.class, () -> new Runner(finish, rounds));
		}

		@Override
		public Receive createReceive() {
			return receiveBuilder().match(String.class, token -> {
				if (count < rounds) {
					count++;
					next.tell(token, getSelf());
				} else {
					finish.tell(new Finish(), getSelf());
				}

			}).match(ActorRef.class, next -> {
				this.next = next;
			}).build();
		}
	}

	private static class Finish {
	}

	private static class StartFinish extends AbstractActor {

		static public Props props() {
			return Props.create(StartFinish.class, () -> new StartFinish());
		}

		private ActorRef starter;
		// private long start;

		@Override
		public Receive createReceive() {
			return receiveBuilder().match(String.class, token -> {
				// System.err.println("Started !!! ");
				// start = System.currentTimeMillis();
				starter.tell(token, getSelf());
			}).match(ActorRef.class, starter -> {
				this.starter = starter;
			}).match(Finish.class, finish -> {
				// long end = System.currentTimeMillis();
				// System.err.println("AKKA Finished !!! " + (end - start));
				context().system().terminate();
			}).build();
		}
	}

}
