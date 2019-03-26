package com.zakgof.aab;

import java.util.BitSet;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class AkkaMassiveTellToActorGroup {

	public static void main(String[] args) throws InterruptedException {
		System.err.println("AKKA Massive Tell started...");
		long start = System.currentTimeMillis();
		run(100000, 100);
		long end = System.currentTimeMillis();
		System.err.println("finished in " + (end - start));
	}

	public static void run(int messagecount, int actorcount) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("akka-massive");
		ActorRef master = system.actorOf(Master.props(messagecount, actorcount));

		master.tell(StartMessage.Start, ActorRef.noSender());
		system.getWhenTerminated().toCompletableFuture().join();
	}
	
	
	private enum StartMessage {
		Start
	}

	private static class Master extends AbstractActor {


		static public Props props(int messagecount, int actorcount) {
			return Props.create(Master.class, () -> new Master(messagecount, actorcount));
		}

		private final int messagecount;
		private final BitSet bitset;
		private final int actorcount;

		public Master(int messagecount, int actorcount) {
			this.messagecount = messagecount;
			this.actorcount = actorcount;
			this.bitset = new BitSet(messagecount * actorcount);
			bitset.set(0, messagecount * actorcount);
		}

		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(StartMessage.class, msg -> start())
				.match(int[].class, this::runnerReplied)
				.build();
		}
		
		private void start() {
			for (int a=0; a<actorcount; a++) {
				ActorRef runner = context().actorOf(Runner.props());
				for (int m=0; m<messagecount; m++) {
					runner.tell(new int[] {a, m}, self());	
				}
			}
		}
		
		private void runnerReplied(int[] msg) {
			int actorNo = msg[0];
			int messageNo = msg[1];
			bitset.clear(actorNo * messagecount + messageNo);
			if (bitset.isEmpty()) {
				context().system().terminate();	
			}
		}
	}
	
	private static class Runner extends AbstractActor {

		static public Props props() {
			return Props.create(Runner.class, Runner::new);
		}
		
		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(int[].class, msg -> run(msg))
				.build();
		}
		
		private void run(int[] msg) {
			sender().tell(msg, self());
		}
	}

}
