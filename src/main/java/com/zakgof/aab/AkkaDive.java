package com.zakgof.aab;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class AkkaDive {

	public static void main(String[] args) throws InterruptedException {
		System.err.println("AKKA Dive started...");
		long start = System.currentTimeMillis();
		run(1000000);
		long end = System.currentTimeMillis();
		System.err.println("finished in " + (end - start));
	}

	public static void run(int actorcount) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("akka-dive");
		final ActorRef master = system.actorOf(Master.props(), "master");

		master.tell(actorcount, ActorRef.noSender());
		system.getWhenTerminated().toCompletableFuture().join();
	}

	private static class FinishMessage {
	}

	private static class Master extends AbstractActor {

		static public Props props() {
			return Props.create(Master.class, Master::new);
		}


		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(Integer.class, limit -> start(limit))
				.match(FinishMessage.class, msg -> finish())
				.build();
		}

		private void start(int limit) {
			ActorRef next = context().actorOf(Runner.props(limit));
			next.tell(1, self());
		}

		private void finish() {
			context().system().terminate();
		}
	}

	private static class Runner extends AbstractActor {

		static public Props props(int limit) {
			return Props.create(Runner.class, () -> new Runner(limit));
		}

		private int limit;

		private Runner(int limit) {
			this.limit = limit;
		}

		private ActorRef prev;

		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(Integer.class, i -> run(i))
				.match(FinishMessage.class, msg -> finish())
				.build();
		}

		private void run(int i) {
			this.prev = sender();
			if (i < limit) {
				ActorRef next = context().system().actorOf(Runner.props(limit));
				next.tell(i+1, self());
			} else {
				prev.tell(new FinishMessage(), self());
			}
		}

		private void finish() {
			prev.tell(new FinishMessage(), self());
		}
	}

}
